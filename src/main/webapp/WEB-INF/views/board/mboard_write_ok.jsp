<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>I CAMP DO IT</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/sweetalert2@11.4.10/dist/sweetalert2.min.css">
<!-- Favicon-->
<link rel="shortcut icon" href="./resources/bootstrap-5/html/img/logo2.svg">
</head>
<body>
 	<!-- Sweet Alert -->
	<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11.4.10/dist/sweetalert2.min.js"></script>
	<script type='text/javascript'>
		
		   var flag = <%=(Integer)request.getAttribute("flag")%>;
					
			if( flag == 0 ) {
				Swal.fire({
					title: '글쓰기 성공',  
					text:	'',
					icon:	'success',
					confirmButtonColor: '#1cb36e', // confrim 버튼 색깔 지정
					confirmButtonText: '확인', // confirm 버튼 텍스트 지정
					
			}).then((result) => {
				
	  			 if (result.isConfirmed) {
	  				 location.href='/mboardlist.do';
	  			 } 
	  		})
	 				
			} else {
				Swal.fire({
					title: '글쓰기 실패',     
					text:	'', 
					icon:	'error',
					confirmButtonColor: '#1cb36e', // confrim 버튼 색깔 지정
					confirmButtonText: '확인', // confirm 버튼 텍스트 지정
					
			}).then((result) => {
				
	  			 if (result.isConfirmed) {
	  				history.back();
	  			 } 
	  		})
				
			}
				 	
</script>
</body>
</html>