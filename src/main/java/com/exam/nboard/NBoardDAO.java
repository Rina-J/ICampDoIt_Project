package com.exam.nboard;

import java.io.File;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class NBoardDAO {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	String url = System.getProperty("user.dir");
	private String nUploadPath = url +"/src/main/webapp/n_upload/";
	
	//list
	public ArrayList<NBoardTO> nboardList() {
		
		String sql = "select seq, writer, subject, title, date_format(wdate, '%Y-%m-%d') wdate, hit from n_board order by seq desc";
		ArrayList<NBoardTO> lists = (ArrayList<NBoardTO>)jdbcTemplate.query(
				sql, new BeanPropertyRowMapper<NBoardTO>(NBoardTO.class) );
		return lists;
		
	}
	
	//NBoardList(페이지)
	public NListTO nList( NListTO listTO ) {
		
		int cpage = listTO.getCpage();
		int recordPerPage = listTO.getRecordPerPage();
		int blockPerPage = listTO.getBlockPerPage();
		
		String sql = "select seq, writer, subject, title, date_format(wdate, '%Y-%m-%d') wdate, hit from n_board order by seq desc";
		ArrayList<NBoardTO> boardLists = (ArrayList<NBoardTO>)jdbcTemplate.query(
				sql, new BeanPropertyRowMapper<NBoardTO>(NBoardTO.class));
		
		listTO.setTotalRecord( boardLists.size() );
		
		listTO.setTotalPage( ( (listTO.getTotalRecord() -1 ) / recordPerPage ) + 1 );		
		int skip = ( cpage - 1 ) * recordPerPage;	
		
		ArrayList<NBoardTO> lists = new ArrayList<NBoardTO>();		
		for( int i=0; i<recordPerPage ; i++ ) {
			if( skip+i != boardLists.size() ) {
				NBoardTO to = new NBoardTO();
				to.setSeq( boardLists.get(skip+i).getSeq() );
				to.setSubject( boardLists.get(skip+i).getSubject() );
				to.setTitle( boardLists.get(skip+i).getTitle() );
				to.setWriter( boardLists.get(skip+i).getWriter() );
				to.setWdate( boardLists.get(skip+i).getWdate() );
				to.setUcode( boardLists.get(skip+i).getUcode() );
				to.setHit( boardLists.get(skip+i).getHit() );
				
				lists.add(to);
				
			} else { break; }
		}
		
		listTO.setBoardLists( lists );
		listTO.setStartBlock( ( ( cpage-1 ) / blockPerPage ) * blockPerPage + 1);
		listTO.setEndBlock( ( (cpage-1) / blockPerPage) * blockPerPage + blockPerPage);
		if(listTO.getEndBlock() >= listTO.getTotalPage()) {
			listTO.setEndBlock(listTO.getTotalPage());
		}		
		return listTO;
		
	}
	
	//NBoardView
	public NBoardTO nboardView(NBoardTO to) {
		
		String sql = "update n_board set hit=hit+1 where seq=?";
		int result = jdbcTemplate.update(sql, to.getSeq() );
		
		sql = "select seq, subject, title, writer, content, date_format(wdate, '%Y-%m-%d') wdate, hit from n_board where seq=?";		
		to = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<NBoardTO>(NBoardTO.class), to.getSeq() );
		
		return to;
		
	}
	
	// write_ok
	public int nboardWriteOk(NBoardTO to) {	
				
		int flag = 1;
		
		String sql = "insert into n_board values  ( 0, ?, ?, ?, ?, now(), 0, ?, ?)";
		int result = jdbcTemplate.update(sql, to.getSubject(), to.getTitle(), to.getWriter(), to.getContent(), to.getUcode(), to.getVcode());
					
		//위에 들어가 부분에 seq값을 다시 들고 나와야 함
		if( result != 1 ) {
			System.out.println("n_board insert 오류");
			return flag;	
		}else {
			flag=0;
		}

		return flag;
	};
		
	//파일이 있으면 진행 없으면 진행 X
	public int nboardWriteFileOk(NBoardTO to, NFileTO fto) {	
		int flag = 1;
		String sql= "";
		String pseq="";
		if(to.getContent().indexOf(fto.getFilename()) != -1) {
			try {
				sql = "select seq from n_board where vcode=?";
				pseq = jdbcTemplate.queryForObject(sql, String.class, to.getVcode());
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				return flag;
			}
						
			sql = "insert into n_file values ( 0, ?, ?, ? )";
			int result = jdbcTemplate.update(sql, pseq, fto.getFilename(), fto.getFilesize());
	
			if( result != 1 ) {
				System.out.println("fileinsert 오류");
				flag = 1;
			}else {
				flag=0;
			}
		
		}else {
			flag=0;
		}
		
		return flag;		
	}
	
	//파일 검사 및 이동
	public void filecnd(NBoardTO to, NFileTO fto) {
		//삭제했을 경우 임시 파일 삭제
		if(to.getContent().indexOf(fto.getFilename()) == -1) {
			String delurl = nUploadPath + fto.getFilename();
			File delFile = new File(delurl);
			if(delFile.exists()) {//파일이 존재하는지 확인
				delFile.delete();
				
			}else {
				System.out.println("파일이 존재 하지 않습니다.");
			}
		
		}
	}
	//글작성하다가 취소 눌렀을 경우 파일 삭제
	public void filedel(String filename) {
		//삭제했을 경우 임시 파일 삭제
		if(filename != "default") {
			String delurl = nUploadPath + filename;
			File delFile = new File(delurl);
			if(delFile.exists()) {//파일이 존재하는지 확인
				delFile.delete();
				
			}else {
				System.out.println("파일이 존재 하지 않습니다.");
			}
		}
	}
	
	//게시글 ucode 찾기
	public NBoardTO findViewUcode(NBoardTO to) {

		String sql = "select ucode from n_board where seq=?";
			to = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<NBoardTO>(NBoardTO.class), to.getSeq() );

		return to;	
	}
	
	//게시글 파일체크
	public ArrayList<NFileTO> nboardDelFileCheck(NBoardTO to) {
		//삭제했을 경우 임시 파일 삭제
		NFileTO fto = new NFileTO();
		ArrayList<NFileTO> nfileArr = new ArrayList<NFileTO>();
		String sql = "select filename from n_file where nseq=?";
		try {
			nfileArr = (ArrayList<NFileTO>)jdbcTemplate.query(sql, new BeanPropertyRowMapper<NFileTO>(NFileTO.class), to.getSeq() );
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			fto.setFilename("null");
			nfileArr.add(fto);
		}
		
		return nfileArr;	
	}
	
	//게시글 삭제메서드
	public Integer fileDBDel(NBoardTO to) {
		//삭제했을 경우 임시 파일 삭제
		int flag = 1;
		
		String sql = "delete from n_file where nseq=?";
		int result = jdbcTemplate.update(sql, to.getSeq());

		if( result != 0 ) {
			flag = 0;
			
		} else {
			System.out.println("filedbDel() 오류");
		}
		
		return flag;
	}
	
	
	// delete_ok
	public int nboardDeleteOk(NBoardTO to) {
		
		int flag = 2;
		
		String sql = "delete from n_board where seq=?";
		int result = jdbcTemplate.update(sql, to.getSeq() );
	
		if(result == 0) {
			System.out.println("nboardDeleteOk 오류");
			flag=1;
		}else {
			flag=0;
		}
	
		return flag;
	}
}
