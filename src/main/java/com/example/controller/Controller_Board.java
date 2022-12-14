package com.example.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.exam.mboard.BoardListTO;
import com.exam.mboard.BoardTO;
import com.exam.mboard.CmtTO;
import com.exam.mboard.FileTO;
import com.exam.login.SignUpTO;
import com.exam.mboard.BoardDAO;


@RestController
public class Controller_Board {
	
	@Autowired
	private BoardDAO dao;
		 	
	String url = System.getProperty("user.dir");
	private String uploadPath = url +"/src/main/webapp/upload/";
	
	@RequestMapping( value="/mboardlist.do" )
	public ModelAndView mboardlist(HttpServletRequest request, HttpSession session) {
		
		int subjectValue = 2;
		if(request.getParameter( "subjectValue" ) != null && !request.getParameter( "subjectValue" ).equals( "" ) ) {
			subjectValue = Integer.parseInt( request.getParameter( "subjectValue" ) );
		}
				
		int cpage = 1;
		if(request.getParameter( "cpage" ) != null && !request.getParameter( "cpage" ).equals( "" ) ) {
			cpage = Integer.parseInt( request.getParameter( "cpage" ) );
		}
		BoardListTO blistTO = new BoardListTO();
		blistTO.setCpage( cpage );		
		
		blistTO = dao.mboardList( blistTO, subjectValue );
		
		ModelAndView modelAndView = new ModelAndView();
		
		if(session.getAttribute("ucode") == null) {
			modelAndView.setViewName( "/login/nousers" );
			return modelAndView;
		}		
		modelAndView.setViewName( "/board/mboard_list" );
		modelAndView.addObject( "blistTO", blistTO );
		modelAndView.addObject( "cpage", cpage );
		modelAndView.addObject( "subjectValue", subjectValue );
		
		return modelAndView;
	}
	
	@RequestMapping( value="/selectmboardlist.do" )
	public String selectmboardlist(HttpServletRequest request, HttpSession session) {
		
		int cpage = 1;
		if(request.getParameter( "cpage" ) != null && !request.getParameter( "cpage" ).equals( "" ) ) {
			cpage = Integer.parseInt( request.getParameter( "cpage" ) );
		}

		BoardTO listTO = new BoardTO();
		listTO.setSubject( String.valueOf(request.getParameter( "subjectValue" )) );
		
		BoardListTO blistTO = new BoardListTO();
		blistTO.setCpage( cpage );
		
		blistTO = dao.mboardSubjectChange(blistTO, listTO);		
		
		StringBuilder sbHtml = new StringBuilder();
		
		ArrayList<BoardTO> freeLists = blistTO.getBoardLists();
		
		for( BoardTO to : freeLists ){
			String seq = to.getSeq();
			String subject = to.getSubject();
			String writer = to.getWriter();
			String title = to.getTitle();
			String wdate = to.getWdate();
			String hit = to.getHit();
			
			sbHtml.append( "<tr onmouseover=\"this.style.background='#f1f6ea'\" onmouseout=\"this.style.background='white'\" style=\"cursor: pointer;\" onclick=\"location.href='mboardview.do?seq=" + seq + "'\">" );
			sbHtml.append( "<td class=\"text-center\">" + seq + "</td>" );
			sbHtml.append( "<td class=\"fw-bold text-center\">" + title + "</td>" );
	        sbHtml.append( "<td></td>" );
	        sbHtml.append( "<td class=\"text-end\">" + writer + "</td>" );
	        sbHtml.append( "<td class=\"text-end\">" + wdate + "</td>" );
	        sbHtml.append( "<td class=\"text-end\">" + hit + "</td>" );
	        sbHtml.append( "</tr>" );
		}
		
		String result = sbHtml.toString();
		
		return result;
	}
	
	@RequestMapping( value="/mboardview.do" )
	public ModelAndView mboardview(HttpServletRequest request, HttpSession session) {
		
		int subjectValue = 2;
		if(request.getParameter( "subjectValue" ) != null && !request.getParameter( "subjectValue" ).equals( "" ) ) {
			subjectValue = Integer.parseInt( request.getParameter( "subjectValue" ) );
		}
		
		BoardTO to = new BoardTO();
		to.setSeq( request.getParameter( "seq" ) );
		
		to = dao.mboardView(to);
		
		BoardListTO bto = new BoardListTO();
		int cpage = Integer.parseInt( request.getParameter( "cpage" ) );
		
		CmtTO cto = new CmtTO();
		cto.setPseq(request.getParameter( "seq" ));
		ArrayList<CmtTO> cmtArr = dao.mboardViewComment(cto);
	
		ModelAndView modelAndView = new ModelAndView();
		
		if(session.getAttribute("ucode") == null) {
			modelAndView.setViewName( "/login/nousers" );
			return modelAndView;
		}
		modelAndView.setViewName( "/board/mboard_view" );
		modelAndView.addObject( "to", to );
		modelAndView.addObject( "cmtArr", cmtArr );
		modelAndView.addObject( "cpage", cpage );
		modelAndView.addObject( "subjectValue", subjectValue );
		
		return modelAndView;
	}
	
	@RequestMapping( value="/mboardwrite.do" )
	public ModelAndView mboardwrite(HttpServletRequest request, HttpSession session) {
		
		int cpage = 1;
		if( request.getParameter("cpage") != null && !request.getParameter("cpage").equals("")) {
			cpage = Integer.parseInt(request.getParameter("cpage"));
		}
		
		BoardListTO listTO = new BoardListTO();
		listTO.setCpage(cpage);
		
		ModelAndView modelAndView = new ModelAndView();
		
		if(session.getAttribute("ucode") == null) {
			modelAndView.setViewName( "/login/nousers" );
			return modelAndView;
		}
		modelAndView.setViewName( "/board/mboard_write" );
		modelAndView.addObject( "cpage", cpage );
		
		return modelAndView;
	}
	
	// ?????? ?????? ??????
	private String getUniqName(String fileName) {	
		
		String tempName = null;
		File file = new File(uploadPath + fileName);
		
		int lastIdx = fileName.lastIndexOf(".");
		int i = 1;
		while(true) {
			if(!file.exists()) {
				break;
			}
			tempName = fileName;
			tempName = tempName.substring(0, lastIdx) + "_" + i + tempName.substring(lastIdx, tempName.length());
			file = new File(uploadPath + tempName);
			i++;
		}
		return file.getName();
	}

	// summernote ????????? ?????????
	@RequestMapping(value = "/imageUpload.do", method=RequestMethod.POST)
	public String uploadSummernoteImage(MultipartFile image, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String savename = image.getOriginalFilename();
						
		String subject = request.getParameter("subject");
		
		long filedata = image.getResource().contentLength();
		String filesize = Long.toString(filedata);
		
		if( subject.equals("1")||subject.equals("2")||subject.equals("3")) {
			savename = getUniqName(savename);
		}
		
		File target = new File(uploadPath + savename);	
				
		response.setContentType("application/json;charset=utf-8");
		
		try {
			FileCopyUtils.copy(image.getBytes(), target);
        } catch (IOException e) {
            FileUtils.forceDelete(target);	// ????????? ????????? ?????? ??????	 
            System.out.println( "[??????] " + e.getMessage() );
        }
		String result= savename + "@" + filesize;
		
		return result;
	}
		
	@RequestMapping(value = "/mboardwrite_ok.do", method=RequestMethod.POST)
	public ModelAndView mboardwriteOk(MultipartFile image, HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		
		SignUpTO sto = new SignUpTO();

		BoardTO to = new BoardTO();
		to.setSubject(request.getParameter("subject"));
		to.setTitle(request.getParameter("title"));
		to.setWriter((String) session.getAttribute("id"));
		to.setContent(request.getParameter("content"));
		to.setUcode((Integer) session.getAttribute("ucode"));
		to.setVcode(request.getParameter("vcode"));
		
		int flag = dao.mboardWriteOk(to);
		
		if(request.getParameter("filename")!="" && request.getParameter("filename")!=null) {
		String[] filenames = request.getParameterValues("filename");
		String[] filesizes = request.getParameterValues("filesize");
		
			for (int i = 0; i < filenames.length; i++) {            
				FileTO fto = new FileTO();
				fto.setFilename(filenames[i]);
				fto.setFilesize(Long.parseLong(filesizes[i]) );
				flag = dao.mboardWriteFileOk(to, fto);
				dao.filecnd(to, fto);
			};
		
		};
		
		ModelAndView modelAndView = new ModelAndView();
		
		if (session.getAttribute("ucode") == null) {
			modelAndView.setViewName("/login/nousers");
			return modelAndView;
		}
		modelAndView.setViewName("/board/mboard_write_ok");
		modelAndView.addObject("flag", flag);

		return modelAndView;
	}

	@RequestMapping( value="/mboardmodify.do" )
	public ModelAndView mboardmodify(HttpServletRequest request, HttpSession session) {
		
		String subjectValue = "";
		if(request.getParameter( "subjectValue" ) != null && !request.getParameter( "subjectValue" ).equals( "" ) ) {
			subjectValue = request.getParameter( "subjectValue" );		
		};
		
		int cpage = 1;
		if(request.getParameter( "cpage" ) != null && !request.getParameter( "cpage" ).equals( "" ) ) {
			cpage = Integer.parseInt( request.getParameter( "cpage" ) );
		}
		
		BoardTO to = new BoardTO();
		FileTO fto = new FileTO();
		ArrayList<FileTO> fileArr = new ArrayList<FileTO>();
		if(subjectValue.equals( "1") || subjectValue.equals( "2") || subjectValue.equals( "3")) {
			to.setUcode((Integer) session.getAttribute("ucode"));
			to.setSeq(request.getParameter( "seq" ));
			to = dao.mboardModify(to);
			
			fileArr = dao.findNFile(to);
		}
		
		ModelAndView modelAndView = new ModelAndView();
		
		if(session.getAttribute("ucode") == null) {
			modelAndView.setViewName( "/login/nousers" );
			return modelAndView;
		}
		modelAndView.setViewName( "/board/mboard_modify" );
		modelAndView.addObject("to", to);
		modelAndView.addObject("fileArr", fileArr);
		modelAndView.addObject("cpage", cpage);
		modelAndView.addObject("subjectValue", subjectValue);
		
		return modelAndView;
	}
	
	@RequestMapping( value="/mboardmodifyOk.do" )
	public String mboardmodifyOk(HttpServletRequest request, HttpSession session) throws IOException {
		
		String subjectValue = "";
		if(request.getParameter( "subject" ) != null && !request.getParameter( "subject" ).equals( "" ) ) {
			subjectValue = request.getParameter( "subject" );		
		};
		
		int cpage = 1;
		if(request.getParameter( "cpage" ) != null && !request.getParameter( "cpage" ).equals( "" ) ) {
			cpage = Integer.parseInt( request.getParameter( "cpage" ) );
		}
				
		int flag = 1;
		
		BoardListTO listTO = new BoardListTO();
		listTO.setCpage(cpage);

		BoardTO to = new BoardTO();
		String modifyUcode = request.getParameter("modifyUcode");	// ?????????ucode
		int ucode = (int)session.getAttribute("ucode");				// ?????????ucode
		
		
		if (  modifyUcode.equals(Integer.toString(ucode)) ) {
				
				FileTO fto = new FileTO();
				
				to.setSeq(request.getParameter( "seq" ));
				to.setSubject(request.getParameter( "subject" ));
				to.setTitle(request.getParameter("title"));
				to.setWriter((String) session.getAttribute("id"));
				to.setContent(request.getParameter("content"));
				to.setUcode((Integer) session.getAttribute("ucode"));
				to.setVcode(request.getParameter("vcode"));
				//????????? ????????????(file??????)
				flag = dao.mboardModifyOk1(to);
				
				// ???????????? ?????? ?????? ??????(????????? ????????? ?????? ????????? ??????)
				if(request.getParameter("filename")!="" && request.getParameter("filename")!=null) {
					String[] filenames = request.getParameterValues("filename");
					String[] filesizes = request.getParameterValues("filesize");
					
					for (int i = 0; i < filenames.length; i++) {            
						FileTO fito = new FileTO();
						fito.setFilename(filenames[i]);
						fito.setFilesize(Long.parseLong(filesizes[i]) );
						flag = dao.mboardModifyOk2(to, fito);
						dao.filecnd(to, fito);
					};
					if(flag==0) {
						System.out.println("???????????? ?????? ??????");
					}else {
						System.out.println("mboardModifyOk2() ??????!");
					}
				};
				//???????????? ????????? ?????? ??????????????????
				if(request.getParameter("newfilename")!=null && request.getParameter("newfilename")!="") {
					String[] newfilenames = request.getParameterValues("newfilename");
					String[] newfilesizes = request.getParameterValues("newfilesize");
					
					for (int i = 0; i < newfilenames.length; i++) {            
						FileTO fito = new FileTO();
						fito.setFilename(newfilenames[i]);
						fito.setFilesize(Long.parseLong(newfilesizes[i]) );
						flag = dao.mboardModifyOk3(to, fito);
						dao.filecnd(to, fito);
						
					};
					if(flag==0) {
						System.out.println("????????? ?????? ?????? ??????");
					}else {
						System.out.println("mboardModifyOk3() ??????!");
					}
					
				};
				
		}		
		return Integer.toString(flag);
	}
	
	@RequestMapping( value="/mboarddelete_ok.do" )
	public ModelAndView mboarddeleteOk(HttpServletRequest request, HttpSession session) throws IOException {
		
		//????????? ?????? ??? ????????? ?????? ??????
		BoardTO to = new BoardTO();
		to.setSeq(request.getParameter("viewseq"));
		
		ArrayList<FileTO> fileArr = new ArrayList<FileTO>();
		//DB??? ?????? ???????????? ????????? ??????
		fileArr=dao.mboardDelFileCheck(to);
		int flag = 2;
		
		//System.out.println( "????????????:" + fileArr.toString() );
		if( fileArr.size() != 0 ) {
			//????????? ?????? => ??????
			System.out.println("????????? ??????");
			//???????????? ????????? ?????? ??????
			for(FileTO fto : fileArr) {
				
				if( !fto.getFilename().equals( "null" ) ) {
					System.out.println( "??????:" + fto.getFilename() );
					dao.filedel(fto.getFilename());
					
				}			
			}			
			
			//DB table?????? ?????? ??????
			 int result= dao.fileDBDel(to);
			 if(result == 0) {
				 flag=0;
			 }else {
				 flag=1;
			 }
		}
		
		if(flag != 1) {
			//???????????? ?????? ?????? ???????????? => ?????? ??????
			//m_cmt??? ???????????? ?????? ??????
			flag=dao.mboardDeleteCmtAll(to);
			if(flag==0) {
				//????????? DB??? ????????? ??????
				flag=dao.mboardDeleteOk(to);
			}
		}else if(flag ==1) {
			System.out.println("?????? ?????? DAO ??????");
		}				
		
		ModelAndView modelAndView = new ModelAndView();
		
		if(session.getAttribute("ucode") == null) {
			modelAndView.setViewName( "/login/nousers" );
			return modelAndView;
		}
		modelAndView.setViewName( "/board/mboard_delete_ok" );
		modelAndView.addObject("flag", flag);
		return modelAndView;
	}
	
	
	@RequestMapping( value="/writecancel.do" )
	public void writecancel(HttpServletRequest request, HttpSession session) {
		
		if(request.getParameter("filename")!="" && request.getParameter("filename")!=null) {
			String[] filenames = request.getParameterValues("filename");
			for (int i = 0; i < filenames.length; i++) {            
				dao.filedel(filenames[i]);
			};
			
		};
	};
	
	@RequestMapping( value="/modifycancel.do" )
	public void modifycancel(HttpServletRequest request, HttpSession session) {
		
		if(request.getParameter("newfilename")!="" && request.getParameter("newfilename")!=null) {
			String[] filenames = request.getParameterValues("newfilename");
			for (int i = 0; i < filenames.length; i++) {            
				dao.filedel(filenames[i]);
			};
			
		};
	};
	
	@RequestMapping( value="/cmt_insert.do" )
	public int cmtInsert(HttpServletRequest request, HttpSession session) throws IOException {
		
		CmtTO cto = new CmtTO();
		cto.setPseq(request.getParameter("seq"));
		cto.setUcode(request.getParameter("ucode"));
		cto.setWriter(request.getParameter("id"));
		cto.setContent(request.getParameter("comment"));
		
		int flag = dao.mboardWriteComment(cto);
		
		return flag;
	}
	
	@RequestMapping( value="/cmtdelete.do" )
	public String cmtList(HttpServletRequest request, HttpSession session) throws IOException, ParseException {
		
		int flag = 1;
	
		String body = (String)request.getParameter("replyseq");
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(body);
		JSONObject data = (JSONObject)obj;	
		
		if(!(session.getAttribute("ucode").toString()).equals(data.get("ucode").toString())) {
			flag=2;
			return Integer.toString(flag);
		}
		
		CmtTO cto = new CmtTO();
		cto.setSeq((String)data.get("seq"));
		
		flag=dao.mboardDeleteComment(cto);
		
		return Integer.toString(flag);
	}
	
}
