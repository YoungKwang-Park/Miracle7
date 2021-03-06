package com.miracle.fts.service;

import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.miracle.fts.DAO.MenuDAO;
import com.miracle.fts.DAO.StoreDAO;
import com.miracle.fts.DAO.StoreFileDAO;
import com.miracle.fts.DTO.MenuDTO;
import com.miracle.fts.DTO.StoreDTO;
import com.miracle.fts.DTO.StoreFileDTO;
import com.miracle.fts.DTO.UploadFileUtils;
import com.miracle.fts.util.SessionScopeUtil;

@Service
public class StoreService {

	
	StoreDAO dao;
    StoreFileDAO storeFileDAO;
	MenuDAO menuDAO;
    
	private SqlSession sqlSession;				
	
	@Autowired
	public void setSqlSession(SqlSession sqlSession) {
		this.sqlSession = sqlSession;
	}
	
	
	
	
	// list
	public List<StoreDTO> list(){ 
		dao = sqlSession.getMapper(StoreDAO.class);
		return dao.select();
	}

	// write
//	public int write(StoreDTO dto) {
//		dao = sqlSession.getMapper(StoreDAO.class);
//		int result = dao.insert(dto);
//		return result;
//	}	
	
	@Transactional
	public int write(MultipartHttpServletRequest mprequest) {
		dao = sqlSession.getMapper(StoreDAO.class);
		storeFileDAO = sqlSession.getMapper(StoreFileDAO.class);
		menuDAO = sqlSession.getMapper(MenuDAO.class);
		
		StoreDTO dto = new StoreDTO();
		
		Integer suid;
		String sname = mprequest.getParameter("sname"); 
		String sbiznum = mprequest.getParameter("sbiznum"); 
		String saddr = mprequest.getParameter("saddr"); 
		String scomt = mprequest.getParameter("scomt");
		String sopinfo = mprequest.getParameter("sopinfo"); 
		Double slat =  Double.parseDouble(mprequest.getParameter("slat")); 
		Double slng = Double.parseDouble(mprequest.getParameter("slng"));
		String spic;
		String sthn;
//		String uid = mprequest.getParameter("uid");
		
		String uid = SessionScopeUtil.getUserId();
		// dto??? ??? ??????
		dto.setSname(sname);
		dto.setSbiznum(sbiznum);
		dto.setSaddr(saddr);
		dto.setScomt(scomt);
		dto.setSopinfo(sopinfo);
		dto.setSlat(slat);
		dto.setSlng(slng);
		dto.setUid(uid);
		
		System.out.println("uid: " + uid);
		// Store??? ?????? ????????? ?????? 
		int result = dao.insert(dto);
		// Store??? ??????????????? ???????????? seq ?????? ?????????
		suid = dto.getSuid();
		System.out.println("suid: " + suid);
		// ?????? ????????? ??????
		// ?????? ?????? ????????????
		Iterator<String> filenames = mprequest.getFileNames();
		// ?????? ???????????? ????????? ???????????? ?????? ????????? ????????? ?????????
		while(filenames.hasNext()) {
			String filename = filenames.next(); // ?????? input name
			MultipartFile file = mprequest.getFile(filename);
			String menu = "menu"; // menu0, menu1 ??? ?????? ????????? ?????? ??????
			MenuDTO menudto = new MenuDTO();
			if(file.getSize() != 0) {
				
				try {
					StoreFileDTO filedto = new StoreFileDTO();
					filedto = UploadFileUtils.uploadFile(file, suid, mprequest); // ????????? ?????? ??????
//				System.out.println("filedto: " + 
//				filedto.getF_name()+" "+
//				filedto.getF_sname()+" "+
//				filedto.getF_thurl()+" "+
//				filedto.getF_url()+" "+
//				filedto.getS_uid()
//				);
					storeFileDAO.addFile(filedto); 		// db??? ?????? ?????? ??????
					
					if(filename.equals("file0")) {		// ????????? ?????? ??????(s_pic)??? ?????? ?????? ???
						spic = filedto.getF_uid().toString();
//						spic = ""+filedto.getF_uid();
						dto.setSpic(spic);
						dao.update(dto);
					}else { 							// ????????????(s_thn)??? ????????? ?????? ???
						menu += filename.substring(4); 	// menu1, menu2
						System.out.println("filename: " + filename);
						System.out.println(menu+": " + mprequest.getParameter(menu));
						menudto.setM_name(mprequest.getParameter(menu)); 
						menudto.setS_uid(suid);
						menudto.setM_pics(filedto.getF_sname());
						menuDAO.insertMenu(menudto);	// ?????? ?????? ??????
					}
					
					result++;
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				System.out.println("????????? ?????????");
			}
			
		}
		
		
		
		
//	    String[] files = StoreDTO.getFiles;

	    
//	        for (String fname : files)
//	            storeFileDAO.addFile(fname);
	        
	        return result;
	}	
    
		
	// view 
	public List<StoreDTO> viewBySuid(int suid){
		dao = sqlSession.getMapper(StoreDAO.class);
		return dao.selectBySuid(suid);
	}	
	
	// update
	public List<StoreDTO> selectBySuid(int suid){
		dao = sqlSession.getMapper(StoreDAO.class); 
		return dao.selectBySuid(suid); 
	}	
		
	// update
//	public int update(StoreDTO storeDTO) {
//		dao = sqlSession.getMapper(StoreDAO.class);
//		return dao.update(storeDTO);	
//	}	
	
	
	// int??? ????????? ????????? 01??? ????????? ??????????????????
	// ?????? ???????????? ??? ??? files/null,???????????? ????????? ????????????
	// int??? ????????? void??? ????????????????????????...
	
	 @Transactional
	 public int update(StoreDTO storeDTO) {
	 	Integer suid = storeDTO.getSuid();
	    String[] files = storeDTO.getFiles();

	    int result = dao.update(storeDTO);
	    storeFileDAO.deleteFiles(suid);

	    for (String fname : files)
	        storeFileDAO.replaceFile(fname, suid);
	    
	    return result;
	}
	
	
	
	// delete
//	public int deleteBySuid(Integer suid) {
//		dao = sqlSession.getMapper(StoreDAO.class);
//		return dao.deleteBySuid(suid);
//	}	
	
    @Transactional
    public int deleteBySuid(Integer suid) {
        //storeFileDAO.deleteFiles(suid);
    	dao = sqlSession.getMapper(StoreDAO.class); 
        return dao.deleteBySuid(suid);        
        
    }

    
	
	
//	
//	    public List<StoreDTO> listAll() throws Exception {
//	        return dao.listAll();
//	    }
//
//	    public List<StoreDTO> listCriteria(Criteria criteria) throws Exception {
//	        return dao.listCriteria(criteria);
//	    }
//
//	    public int countStores(Criteria criteria) throws Exception {
//	        return dao.countStores(criteria);
//	    }
//
//	    public List<StoreDTO> listSearch(SearchCriteria searchCriteria) throws Exception {
//	        return dao.listSearch(searchCriteria);
//	    }
//
//	    public int countSearchedStores(SearchCriteria searchCriteria) throws Exception {
//	        return dao.countSearchedStores(searchCriteria);
//	    }
//	
	
	
}














