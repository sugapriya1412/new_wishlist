package org.vtop.CourseRegistration.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.vtop.CoureRegistration.mongo.model.CourseCatalog;
import org.vtop.CourseRegistration.model.CourseAllocationModel;
import org.vtop.CourseRegistration.model.CourseCatalogModel;
import org.vtop.CourseRegistration.model.SlotTimeMasterModel;
import org.vtop.CourseRegistration.service.CourseCatalogService;
import org.vtop.CourseRegistration.service.CourseRegistrationCommonFunction;
import org.vtop.CourseRegistration.service.CourseRegistrationReadWriteService;
import org.vtop.CourseRegistration.service.CourseRegistrationService;
import org.vtop.CourseRegistration.service.SemesterMasterService;
import org.vtop.CourseRegistration.service.StudentHistoryService;
import org.vtop.CourseRegistration.service.WishlistRegistrationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vtop.CourseRegistration.mongo.service.CourseCatalogMongoService;
import org.vtop.CourseRegistration.mongo.service.CourseRegistrationCommonMongoService;




@Controller
public class CourseRegistrationFormController 
{	
	@Autowired private CourseCatalogService courseCatalogService;
	@Autowired private CourseRegistrationService courseRegistrationService;
	@Autowired private StudentHistoryService studentHistoryService;
	@Autowired private CourseRegistrationCommonFunction courseRegCommonFn;
	@Autowired private SemesterMasterService semesterMasterService;
	@Autowired private WishlistRegistrationService wishlistRegistrationService;
	@Autowired private CourseRegistrationReadWriteService courseRegistrationReadWriteService;
	
	@Autowired private CourseCatalogMongoService courseCatalogMongoService;
	@Autowired private CourseRegistrationCommonMongoService courseRegistrationCommonMongoService;
	
	private static final Logger logger = LogManager.getLogger(CourseRegistrationFormController.class);	
	private static final String[] classType = { "BFS" };
	private static final String RegErrorMethod = "FS-2223-WL";
	private static final List<String> crCourseOption = new ArrayList<String>(Arrays.asList("RGR","RGCE","RGP","RGW","RPCE","RWCE","RR"));
	
	private static final String CAMPUSCODE = "CHN";	
	private static final int BUTTONS_TO_SHOW = 5;
	private static final int INITIAL_PAGE = 0;
	private static final int INITIAL_PAGE_SIZE = 5;
	private static final int[] PAGE_SIZES = { 5, 10, 15, 20 };
	
	
	@PostMapping("viewRegistrationOption")
	public String viewRegistrationOption(Model model, HttpSession session, HttpServletRequest request, 
						HttpServletResponse response) 
	{
		String IpAddress = (String) session.getAttribute("IpAddress");
		String msg = null, infoMsg = "", urlPage = "";
		Integer updateStatus = 1;		
		int allowStatus = 2, regularFlag = 2,reRegFlag = 2;
		@SuppressWarnings("unchecked")
		List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
				
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		Integer PEUEAllowStatus = (Integer) session.getAttribute("PEUEAllowStatus");
		String programGroupCode = (String) session.getAttribute("ProgramGroupCode");
		regularFlag = (Integer) session.getAttribute("regularFlag");
		reRegFlag =  (Integer) session.getAttribute("reRegFlag");
		Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				
		model.addAttribute("regularFlag", regularFlag);
		model.addAttribute("PEUEAllowStatus", PEUEAllowStatus);
				
		try
		{			
			if (registerNumber != null)
			{				
				Integer maxCredit = (Integer) session.getAttribute("maxCredit");
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer semesterId  = (Integer) session.getAttribute("SemesterId");
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String pOldRegisterNumber = (String) session.getAttribute("OldRegNo"); 
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				Integer compulsoryCourseStatus = (Integer) session.getAttribute("compulsoryCourseStatus");
				String registrationMethod = (String) session.getAttribute("registrationMethod");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
												
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
							
				
				String returnVal = courseRegistrationReadWriteService.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				int compulsoryStatus = 2;
				String registrationOption = "";
				Integer pageSize = 5;
				Integer page = 1;
				Integer searchType = 0;
				String searchVal = "";
				String subCourseOption = "";
								
				switch(allowStatus)
				{
					case 1:
						if (compulsoryCourseStatus == 1)
						{
							compulsoryStatus = courseRegCommonFn.compulsoryCourseCheck(programGroupId, studyStartYear, 
													StudentGraduateYear, semesterId, semesterSubId, registerNumber, 
													classGroupId, classType, ProgramSpecCode, programSpecId, 
													programGroupCode, pOldRegisterNumber, compCourseList, costCentreCode);
							session.setAttribute("compulsoryCourseStatus", compulsoryStatus);
						}
						
						if (compulsoryStatus == 1)
						{
							registrationOption = "COMP";
							getCompulsoryCourseList(registrationOption, pageSize, page, searchType, searchVal, 
									subCourseOption, session, model, compCourseList);
							session.setAttribute("registrationOption", registrationOption);
							
							urlPage = "mainpages/CompulsoryCourseList :: section";
						}
						else
						{
							session.removeAttribute("registrationOption");
														
							model.addAttribute("regOptionList", courseRegCommonFn.getRegistrationOption(programGroupCode, 
									registrationMethod, regularFlag, reRegFlag, PEUEAllowStatus, programSpecId, studyStartYear, 
									curriculumVersion));
							//model.addAttribute("regOptionList", courseRegistrationCommonMongoService.getRegistrationOption(
								//	programGroupCode, registrationMethod, regularFlag, reRegFlag, PEUEAllowStatus, programSpecId, 
								//	studyStartYear, curriculumVersion));
							
							model.addAttribute("studySystem", session.getAttribute("StudySystem"));
							model.addAttribute("maxCredit", maxCredit);
							model.addAttribute("showFlag", 0);
							
							
							urlPage = "mainpages/RegistrationOptionList :: section";
						}
						break;
					
					default:						
						msg = infoMsg;						
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						urlPage = "redirectpage";
						return urlPage;
				}		
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		}
		catch(Exception ex)
		{
			logger.trace(ex);
			
			courseRegistrationReadWriteService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationDeleteController", 
					"processDeleteCourseRegistration", registerNumber, IpAddress);
			courseRegistrationReadWriteService.updateRegistrationLogLogoutTimeStamp2(IpAddress,registerNumber);
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			
			return urlPage;			
		}
		model.addAttribute("info", msg);
		
		return urlPage;
	}
	
	
	@PostMapping("processFFCStoCal")
	public String processFFCStoCal(Model model, HttpServletRequest request, HttpSession session) 
	{	
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		
		String msg = null, urlPage = "", infoMsg = "", subCourseOption ="", registrationOption = "FFCSCAL";
		int allowStatus = 2;		
		Integer updateStatus = 1, page = 1;		
				
		try
		{				
			if (registerNumber != null)
			{	
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegistrationReadWriteService.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];		
				
				switch (allowStatus)
				{
					case 1:
						urlPage = processRegistrationOption(registrationOption, model, session, 5, page, 0, "NONE", 
									subCourseOption, request);
						break;
						
					default:
						msg = infoMsg;
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						urlPage = "redirectpage";
						return urlPage;
				}
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		}
		catch(Exception e)
		{
			logger.trace(e);
			
			courseRegistrationReadWriteService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationDeleteController", 
					"processDeleteCourseRegistration", registerNumber, IpAddress);
			courseRegistrationReadWriteService.updateRegistrationLogLogoutTimeStamp2(IpAddress,registerNumber);
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			
			return urlPage;
		}		
		model.addAttribute("info", msg);
		
		return urlPage;
	}	
	

	@PostMapping("processRegistrationOption")
	public String processRegistrationOption(@RequestParam(value = "registrationOption", required = false) String registrationOption, 
						Model model, HttpSession session, @RequestParam(value = "pageSize", required = false) Integer pageSize,
						@RequestParam(value = "page", required = false) Integer page,
						@RequestParam(value = "searchType", required = false) Integer searchType,
						@RequestParam(value = "searchVal", required = false) String searchVal,
						@RequestParam(value = "subCourseOption", required = false) String subCourseOption, HttpServletRequest request)
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		
		String flagValue = request.getParameter("flag");
		if ((flagValue == null) || (flagValue.equals(null)))
		{
			flagValue = "0";
		}
		
		String msg = null, infoMsg = "", urlPage = "";				
		Integer updateStatus = 1;
		int allowStatus = 2;
				
		if ((registrationOption != null) && (!registrationOption.equals(null))) 
		{
			session.setAttribute("registrationOption", registrationOption);
		} 
		else 
		{
			registrationOption = (String) session.getAttribute("registrationOption");
		}
				
		try
		{
			if (registerNumber != null)
			{
				session.setAttribute("pageAuthKey", courseRegCommonFn.generatePageAuthKey(registerNumber, 2));
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				Integer semesterId  = (Integer) session.getAttribute("SemesterId");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String pOldRegisterNumber = (String) session.getAttribute("OldRegNo");
				
				@SuppressWarnings("unchecked")
				List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				Integer compulsoryCourseStatus = (Integer) session.getAttribute("compulsoryCourseStatus");
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegistrationReadWriteService.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];	
				
				int compulsoryStatus = 2;
				
				logger.trace("\n allowStatus: "+ allowStatus);
				switch(allowStatus)
				{
					case 1:
						logger.trace("\n compulsoryCourseStatus: "+ compulsoryCourseStatus);
						if (compulsoryCourseStatus == 1)
						{
							compulsoryStatus = courseRegCommonFn.compulsoryCourseCheck(programGroupId, studyStartYear, 
													StudentGraduateYear, semesterId, semesterSubId, registerNumber, 
													classGroupId, classType, ProgramSpecCode, programSpecId, 
													ProgramGroupCode, pOldRegisterNumber, compCourseList, costCentreCode);
							session.setAttribute("compulsoryCourseStatus", compulsoryStatus);
						}
						
						logger.trace("\n compulsoryStatus: "+ compulsoryStatus);
						if (compulsoryStatus == 1)
						{	
							getCompulsoryCourseList(registrationOption, pageSize, page, searchType, searchVal, 
									subCourseOption, session, model, compCourseList);
							urlPage = "mainpages/CompulsoryCourseList :: section";
						}
						else
						{
							callCourseRegistrationTypes(registrationOption, pageSize, page, searchType, searchVal, session, model);
							model.addAttribute("studySystem", session.getAttribute("StudySystem"));
							model.addAttribute("registrationOption", registrationOption);					
							model.addAttribute("showFlag", 1);
							
							switch (flagValue)
							{
								case "1":
									urlPage = "mainpages/CourseList :: cclistfrag";
									break;
								default:
									urlPage = "mainpages/CourseList :: section";
									break;
							}
						}
						break;
						
					default:
						msg = infoMsg;
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						urlPage = "redirectpage";
						return urlPage;						
					}					
				}			
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		} 
		catch (Exception ex) 
		{
			logger.trace(ex);
			
			courseRegistrationReadWriteService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationDeleteController", 
					"processDeleteCourseRegistration", registerNumber, IpAddress);
			courseRegistrationReadWriteService.updateRegistrationLogLogoutTimeStamp2(IpAddress,registerNumber);
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			
			return urlPage;
		}
		
		return urlPage;
	}
	
	public int callCourseRegistrationTypes(String registrationOption, Integer pageSize, Integer page, 
					Integer searchType, String searchVal, HttpSession session, Model model)
	{
		String semesterSubId = (String) session.getAttribute("SemesterSubId");		
		String registerNo = (String) session.getAttribute("RegisterNumber");
		
		try
		{
			if (semesterSubId != null)
			{
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				Integer ProgramSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				Integer studYear = (Integer) session.getAttribute("StudyStartYear");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
				
				@SuppressWarnings("unchecked")
				List<Integer> egbGroupId = (List<Integer>) session.getAttribute("EligibleProgramLs");
				String[] courseSystem = (String[]) session.getAttribute("StudySystem");				
				String[] registerNumber = (String[]) session.getAttribute("registerNumberArray");				
				String registrationMethod = (String) session.getAttribute("registrationMethod");
				Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				Integer PEUEAllowStatus = (Integer) session.getAttribute("PEUEAllowStatus");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
											
				Pager pager = null;		
				int evalPageSize = INITIAL_PAGE_SIZE;
				int evalPage = INITIAL_PAGE;
				evalPageSize = pageSize == null ? INITIAL_PAGE_SIZE : pageSize;
				evalPage = (page == null || page < 1) ? INITIAL_PAGE : page - 1;
				int pageSerialNo = evalPageSize * evalPage;
				int srhType = (searchType == null) ? 0 : searchType;
				String srhVal = (searchVal == null) ? "NONE" : searchVal;
								
				logger.trace("pageSize: "+ pageSize +" | page: "+ page +" | pageSerialNo: "+ pageSerialNo 
						+" | evalPageSize: "+ evalPageSize +" | evalPage: "+ evalPage);
				
				if (registrationOption != null) 
				{
					session.setAttribute("registrationOption", registrationOption);
				} 
				else 
				{
					registrationOption = (String) session.getAttribute("registrationOption");
				}
				
				int totalPage = 0, pageNumber = evalPage; 
				String[] pagerArray = new String[]{};
				
				List<CourseCatalogModel> courseCatalogModelPageList = new ArrayList<CourseCatalogModel>();
				courseCatalogModelPageList = courseCatalogService.getCourseListForRegistration(registrationOption, 
												CAMPUSCODE, courseSystem, egbGroupId, programGroupId, semesterSubId, 
												ProgramSpecId, classGroupId, classType, studYear, curriculumVersion, 
												registerNo, srhType, srhVal, StudentGraduateYear, ProgramGroupCode, 
												ProgramSpecCode, registrationMethod, registerNumber, PEUEAllowStatus, 
												evalPage, evalPageSize, costCentreCode);
				
			/*	List<CourseCatalog> courseCatalogModelPageList = new ArrayList<>();
				courseCatalogModelPageList = courseCatalogMongoService.getCourseListForRegistration(registrationOption, 
												CAMPUSCODE, courseSystem, egbGroupId, programGroupId, semesterSubId, 
												ProgramSpecId, classGroupId, classType, studYear, curriculumVersion, 
												registerNo, srhType, srhVal, StudentGraduateYear, ProgramGroupCode, 
												ProgramSpecCode, registrationMethod, registerNumber, PEUEAllowStatus, 
												evalPage, evalPageSize, costCentreCode);*/
				
				logger.trace("CourseListSize: "+ courseCatalogModelPageList.size() 
							+" | evalPageSize: "+ evalPageSize +" | pageNumber: "+ pageNumber);
				
				pagerArray = courseCatalogService.getTotalPageAndIndex(courseCatalogModelPageList.size(), 
								evalPageSize, pageNumber).split("\\|");
				totalPage = Integer.parseInt(pagerArray[0]);
				pager = new Pager(totalPage, pageNumber, BUTTONS_TO_SHOW);
				logger.trace("totalPage: "+ totalPage);
							
				model.addAttribute("tlTotalPage", totalPage);
				model.addAttribute("tlPageNumber", pageNumber);
				model.addAttribute("tlCourseCatalogModelList", courseCatalogModelPageList);
				model.addAttribute("courseRegModelList", wishlistRegistrationService.getCourseByRegisterNumberAndClassGroup(
						semesterSubId, classGroupId, registerNo));
				model.addAttribute("registrationOption", registrationOption);
				model.addAttribute("pageSlno", pageSerialNo);
				model.addAttribute("selectedPageSize", evalPageSize);
				model.addAttribute("pageSizes", PAGE_SIZES);
				model.addAttribute("srhType", srhType);
				model.addAttribute("srhVal", srhVal);
				model.addAttribute("pager", pager);
				model.addAttribute("page", page);
			}			
		}
		catch(Exception e)
		{
			logger.trace(e);
			session.invalidate();
		}
				
		return 1;
	}
	

	@PostMapping(value="processCourseRegistration")
	public String processCourseRegistration(String courseId, 
						@RequestParam(value = "page", required = false) Integer page,
						@RequestParam(value = "searchType", required = false) Integer searchType,
						@RequestParam(value = "searchVal", required = false) String searchVal, 
						Model model, HttpSession session, HttpServletRequest request)//M1 
	{			
		String IpAddress = (String) session.getAttribute("IpAddress");
		String semesterSubId = (String) session.getAttribute("SemesterSubId");
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		
		String urlPage = "", courseTypeDisplay = "", msg = null, message = null, courseOption = "",	
					genericCourseType = "", infoMsg = "", ccCourseSystem = "", crSubCourseOption = "",  
					crSubCourseType = "", crSubCourseDate = "";
		String courseCategory = "NONE", subCourseType = "", subCourseDate = "", courseCode = "", 
					genericCourseTypeDisplay = "", authKeyVal = "", corAuthStatus = "", ccCourseId = "", 
					crCourseId = "", crCourseCode = "", crGenericCourseType = "";
		String[] regStatusArr = new String[50], regStatusArr2 = new String[50], tempRegStatusArr = new String[50];
		Integer updateStatus = 1;
		int allowStatus = 2, regStatusFlag = 2, projectStatus = 2, regAllowFlag = 1, wlAllowFlag = 1, 
				audAllowFlag = 1, rgrAllowFlag=2, minAllowFlag = 2, honAllowFlag = 2, adlAllowFlag = 2, 
				RPEUEAllowFlag=2, csAllowFlag=2, RUCUEAllowFlag=2;
		int ethExistFlag = 2, epjExistFlag = 2, epjSlotFlag = 2, regularFlag=2, crCourseStatus = 2;
				
		try
		{			
			if (semesterSubId != null)
			{						
				registerNumber = (String) session.getAttribute("RegisterNumber");					
				String[] pCourseSystem = (String[]) session.getAttribute("StudySystem");
				Integer pProgramGroupId = (Integer) session.getAttribute("ProgramGroupId"); 
				String pProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				Integer pProgramSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String pProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				
				String pSemesterSubId = (String) session.getAttribute("SemesterSubId"); 
				Integer pSemesterId = (Integer) session.getAttribute("SemesterId");
				Float CurriculumVersion = (Float) session.getAttribute("curriculumVersion");
				String pOldRegisterNumber = (String) session.getAttribute("OldRegNo"); 
				Integer maxCredit = (Integer) session.getAttribute("maxCredit");
				Integer cclTotalCredit = (Integer) session.getAttribute("cclTotalCredit");
				
				String registrationOption = (String) session.getAttribute("registrationOption");
				String subCourseOption = (String) session.getAttribute("subCourseOption");
				Integer StudyStartYear = (Integer) session.getAttribute("StudyStartYear");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
				Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				Integer OptionNAStatus=(Integer) session.getAttribute("OptionNAStatus");
				
				Integer PEUEAllowStatus = (Integer) session.getAttribute("PEUEAllowStatus");
				String studentStudySystem = (String) session.getAttribute("studentStudySystem");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String programGroupMode = (String) session.getAttribute("programGroupMode");
				String studentCgpaData = (String) session.getAttribute("studentCgpaData");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				Integer acadGraduateYear = (Integer) session.getAttribute("acadGraduateYear");
				
				regularFlag = (Integer) session.getAttribute("regularFlag");
				session.setAttribute("corAuthStatus", "NONE");
				session.setAttribute("authStatus", "NONE");
				session.setAttribute("courseCategory", "");

				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
					
				String returnVal = courseRegistrationReadWriteService.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				List<String> courseTypeArr = new ArrayList<String>();					
				@SuppressWarnings("unchecked")
				List<String> registerNumberList = (List<String>) session.getAttribute("registerNumberList");
				@SuppressWarnings("unchecked")
				List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
				
				CourseCatalogModel courseCatalog = new CourseCatalogModel();
				CourseCatalogModel courseCatalog2 = null;
								
				courseCatalog = courseCatalogService.getOne(courseId);
				if (courseCatalog != null)
				{
					courseCode = courseCatalog.getCode();
					genericCourseType = courseCatalog.getGenericCourseType();
					genericCourseTypeDisplay = courseCatalog.getCourseTypeComponentModel().getDescription();
					ccCourseSystem = courseCatalog.getCourseSystem();
					
					if ((courseCatalog.getCorequisite() != null) && (!courseCatalog.getCorequisite().equals("")) 
							&& (!courseCatalog.getCorequisite().equals("NONE")) && (!courseCatalog.getCorequisite().equals("NIL")))
					{
						crCourseCode = courseCatalog.getCorequisite().trim();
					}
				}
				logger.trace("\n courseCatalog: "+ courseCatalog.toString());
				
				if ((!ccCourseSystem.equals("NONFFCS")) && (!ccCourseSystem.equals("FFCS")) 
						&& (!ccCourseSystem.equals("CAL")) && (!crCourseCode.equals("")) 
						&& (!crCourseCode.equals("NONE")))
				{	
					crCourseStatus = 1;
										
					courseCatalog2 = courseCatalogService.getLatestVersionByCourseCode(crCourseCode);
					if (courseCatalog2 != null)
					{
						crCourseId = courseCatalog2.getCourseId();
						crGenericCourseType = courseCatalog2.getGenericCourseType();
					}
				}
				
				logger.trace(pCourseSystem +" | "+ pProgramGroupId +" | "+ pProgramGroupCode 
						+" | "+ pProgramSpecCode +" | "+ pSemesterSubId +" | "+ registerNumber 
						+" | "+ pOldRegisterNumber +" | "+ maxCredit +" | "+ courseId 
						+" | "+ StudyStartYear+" | "+ StudentGraduateYear +" | "+ studentStudySystem);
				
				switch(allowStatus)
				{
					case 1:
						tempRegStatusArr = courseRegCommonFn.CheckRegistrationCondition(pCourseSystem, pProgramGroupId, 
												pProgramGroupCode, pProgramSpecCode, pSemesterSubId, registerNumber, 
												pOldRegisterNumber, maxCredit, courseId, StudyStartYear, StudentGraduateYear, 
												studentStudySystem, pProgramSpecId, CurriculumVersion, PEUEAllowStatus, 
												programGroupMode, classGroupId, studentCgpaData, OptionNAStatus, compCourseList, 
												pSemesterId, classType, costCentreCode, acadGraduateYear, cclTotalCredit).split("/");
						if ((Integer.parseInt(tempRegStatusArr[0]) == 1) && (crCourseStatus == 1))
						{
							if (crCourseOption.contains(tempRegStatusArr[2]))
							{
								regStatusArr2 = courseRegCommonFn.CheckRegistrationCondition(pCourseSystem, pProgramGroupId, 
													pProgramGroupCode, pProgramSpecCode, pSemesterSubId, registerNumber, 
													pOldRegisterNumber, maxCredit, crCourseId, StudyStartYear, StudentGraduateYear, 
													studentStudySystem, pProgramSpecId, CurriculumVersion, PEUEAllowStatus, 
													programGroupMode, classGroupId, studentCgpaData, OptionNAStatus, compCourseList, 
													pSemesterId, classType, costCentreCode, acadGraduateYear, cclTotalCredit).split("/");
								if ((Integer.parseInt(regStatusArr2[0]) == 1) && (crCourseOption.contains(regStatusArr2[2])))
								{
									crCourseStatus = 1;
									
									if (genericCourseType.equals("LO") && crGenericCourseType.equals("TH"))
									{
										regStatusArr = regStatusArr2;
									}
									else
									{
										regStatusArr = tempRegStatusArr;
									}
								}
								else
								{
									regStatusArr = tempRegStatusArr;
									crCourseStatus = 2;
								}
							}
							else
							{
								regStatusArr = tempRegStatusArr;
								crCourseStatus = 2;
							}
						}
						else
						{
							regStatusArr = tempRegStatusArr;
							crCourseStatus = 2;
						}
						
						regStatusFlag = Integer.parseInt(regStatusArr[0]);
						message = regStatusArr[1];
						courseOption = regStatusArr[2];
						regAllowFlag = Integer.parseInt(regStatusArr[3]);
						wlAllowFlag = Integer.parseInt(regStatusArr[4]);
						audAllowFlag = Integer.parseInt(regStatusArr[8]);
						rgrAllowFlag= Integer.parseInt(regStatusArr[11]);
						minAllowFlag = Integer.parseInt(regStatusArr[13]);
						honAllowFlag = Integer.parseInt(regStatusArr[12]);
						courseCategory = regStatusArr[14];
						adlAllowFlag = Integer.parseInt(regStatusArr[15]);
						authKeyVal = regStatusArr[16];
						RPEUEAllowFlag = Integer.parseInt(regStatusArr[17]);
						csAllowFlag = Integer.parseInt(regStatusArr[18]);
						RUCUEAllowFlag = Integer.parseInt(regStatusArr[19]);
						ccCourseId = regStatusArr[20];
						
						if ((crCourseStatus == 1) && genericCourseType.equals("TH") && crGenericCourseType.equals("LO"))
						{
							corAuthStatus = regStatusArr[2] +"/"+ regStatusArr[3] +"/"+ regStatusArr[4] +"/"+ regStatusArr[8] 
												+"/"+ regStatusArr[11] +"/"+ regStatusArr[13] +"/"+ regStatusArr[12] +"/"+ regStatusArr[14] 
												+"/"+ regStatusArr[15] +"/"+ regStatusArr[17] +"/"+ regStatusArr[6] +"/"+ regStatusArr[7] 
												+"/"+ regStatusArr[9] +"/"+ regStatusArr[10] +"/"+ regStatusArr[18]	+"/"+ regStatusArr[19] 
												+"/"+ regStatusArr[20] +"/"+ crCourseStatus +"/"+ crCourseId +"/"+ crCourseCode 
												+"/"+ crGenericCourseType +"/"+ regStatusArr2[7] +"/"+ regStatusArr2[9] +"/"+ regStatusArr2[10];
						}
						else if ((crCourseStatus == 1) && genericCourseType.equals("LO") && crGenericCourseType.equals("TH"))
						{
							corAuthStatus = regStatusArr[2] +"/"+ regStatusArr[3] +"/"+ regStatusArr[4] +"/"+ regStatusArr[8] 
												+"/"+ regStatusArr[11] +"/"+ regStatusArr[13] +"/"+ regStatusArr[12] +"/"+ regStatusArr[14] 
												+"/"+ regStatusArr[15] +"/"+ regStatusArr[17] +"/"+ regStatusArr[6] +"/"+ regStatusArr[7] 
												+"/"+ regStatusArr[9] +"/"+ regStatusArr[10] +"/"+ regStatusArr[18] +"/"+ regStatusArr[19] 
												+"/"+ regStatusArr[20] +"/"+ crCourseStatus +"/"+ courseId +"/"+ courseCode 
												+"/"+ genericCourseType +"/"+ tempRegStatusArr[7] +"/"+ tempRegStatusArr[9] +"/"+ tempRegStatusArr[10];
						}
						else
						{
							corAuthStatus = regStatusArr[2] +"/"+ regStatusArr[3] +"/"+ regStatusArr[4] +"/"+ regStatusArr[8] 
												+"/"+ regStatusArr[11] +"/"+ regStatusArr[13] +"/"+ regStatusArr[12] +"/"+ regStatusArr[14] 
												+"/"+ regStatusArr[15] +"/"+ regStatusArr[17] +"/"+ regStatusArr[6] +"/"+ regStatusArr[7] 
												+"/"+ regStatusArr[9] +"/"+ regStatusArr[10] +"/"+ regStatusArr[18] +"/"+ regStatusArr[19] 
												+"/"+ regStatusArr[20] +"/"+ crCourseStatus +"/"+ crCourseId +"/"+ crCourseCode 
												+"/"+ crGenericCourseType +"///";
						}
			
						session.setAttribute("authStatus", authKeyVal);
						session.setAttribute("corAuthStatus", corAuthStatus);
						//logger.trace("\n courseOption: "+ courseOption +" | crCourseStatus: "+ crCourseStatus 
						//		+" | genericCourseType: "+ genericCourseType +" | crGenericCourseType: "+ crGenericCourseType);
						
																		
						switch(courseOption)
						{
							case "RR":
							case "RRCE":									
								if (!regStatusArr[6].equals("NONE"))
								{
									//courseTypeArr = Arrays.asList(regStatusArr[6].split(","));
									
									if ((crCourseStatus == 1) && genericCourseType.equals("TH") && crGenericCourseType.equals("LO"))
									{
										courseTypeArr.addAll(semesterMasterService.getCourseTypeComponentByGenericType(genericCourseType));
										courseTypeArr.addAll(semesterMasterService.getCourseTypeComponentByGenericType(crGenericCourseType));
									}
									else if ((crCourseStatus == 1) && genericCourseType.equals("LO") && crGenericCourseType.equals("TH"))
									{
										courseTypeArr.addAll(semesterMasterService.getCourseTypeComponentByGenericType(crGenericCourseType));
										courseTypeArr.addAll(semesterMasterService.getCourseTypeComponentByGenericType(genericCourseType));
									}
									else
									{
										courseTypeArr = Arrays.asList(regStatusArr[6].split(","));
									}
								}																	
								
								if (courseTypeArr.size() <= 0)
								{
									courseTypeArr = semesterMasterService.getCourseTypeComponentByGenericType(genericCourseType);
								}
								break;
							
							default:
								if ((crCourseStatus == 1) && genericCourseType.equals("TH") && crGenericCourseType.equals("LO"))
								{
									courseTypeArr.addAll(semesterMasterService.getCourseTypeComponentByGenericType(genericCourseType));
									courseTypeArr.addAll(semesterMasterService.getCourseTypeComponentByGenericType(crGenericCourseType));
								}
								else if ((crCourseStatus == 1) && genericCourseType.equals("LO") && crGenericCourseType.equals("TH"))
								{
									courseTypeArr.addAll(semesterMasterService.getCourseTypeComponentByGenericType(crGenericCourseType));
									courseTypeArr.addAll(semesterMasterService.getCourseTypeComponentByGenericType(genericCourseType));
								}
								else
								{
									courseTypeArr.addAll(semesterMasterService.getCourseTypeComponentByGenericType(genericCourseType));
								}
								break;
						}
						//logger.trace("\n courseTypeArr: "+ courseTypeArr.toString());
							
						switch(courseOption)
						{
							case "RR":
							case "RRCE":
							case "GI":
							case "GICE":
							case "RGCE":
							case "RPCE":
							case "RWCE":
								//subCourseOption = regStatusArr[7];
								//subCourseType = regStatusArr[9];
								//subCourseDate = regStatusArr[10];
								
								if ((crCourseStatus == 1) && genericCourseType.equals("TH") && crGenericCourseType.equals("LO"))
								{
									subCourseOption = regStatusArr[7];
									subCourseType = regStatusArr[9];
									subCourseDate = regStatusArr[10];
									crSubCourseOption = regStatusArr2[7]; 
									crSubCourseType = regStatusArr2[9];
									crSubCourseDate = regStatusArr2[10];	
								}
								else if ((crCourseStatus == 1) && genericCourseType.equals("LO") && crGenericCourseType.equals("TH"))
								{
									subCourseOption = regStatusArr[7];
									subCourseType = regStatusArr[9];
									subCourseDate = regStatusArr[10];
									crSubCourseOption = tempRegStatusArr[7]; 
									crSubCourseType = tempRegStatusArr[9];
									crSubCourseDate = tempRegStatusArr[10];
								}
								else
								{
									subCourseOption = regStatusArr[7];
									subCourseType = regStatusArr[9];
									subCourseDate = regStatusArr[10];
								}
								logger.trace("\n subCourseOption: "+ subCourseOption +" | subCourseType: "+ subCourseType 
										+" | subCourseDate: "+ subCourseDate);
								break;
								
							default:
								if (regStatusArr[7].equals("NONE"))
									
								{
									subCourseOption = "";
								}
								break;
						}
								
						for (String crstp : courseTypeArr) 
						{
							logger.trace("\n crstp: "+ crstp);
							if (courseTypeDisplay.equals(""))
							{
								courseTypeDisplay = semesterMasterService.getCourseTypeMasterByCourseType(crstp).getDescription();
							}
							else
							{
								courseTypeDisplay = courseTypeDisplay +" / "+ semesterMasterService.getCourseTypeMasterByCourseType(crstp).getDescription();
							}
								
							if (crstp.equals("ETH"))
							{
								ethExistFlag = 1;
							}
							else if (crstp.equals("EPJ"))
							{
								epjExistFlag = 1;
							}
						}
							
						if ((courseTypeArr.size() == 2) && (genericCourseType.equals("ETLP")) 
								&& (ethExistFlag == 1) && (epjExistFlag == 1))
						{
							epjSlotFlag = 1;
						}
						else if ((courseTypeArr.size() == 1) && (epjExistFlag == 1))
						{
							epjSlotFlag = 1;
						}
						logger.trace("regStatusFlag: "+ regStatusFlag);
															
						switch(regStatusFlag)
						{    
							case 1:									
								if (projectStatus == 1)
								{
									List<Object[]> courseCostCentre = semesterMasterService.getEmployeeProfileByCampusCode(CAMPUSCODE);
									
									model.addAttribute("courseCostCentre", courseCostCentre);
									model.addAttribute("ProgramCode", session.getAttribute("ProgramGroupCode"));
									model.addAttribute("courseOption", courseOption);										
									
									urlPage = "mainpages/ProjectRegistration :: section";
								}
								else
								{											
									urlPage = "mainpages/CourseRegistration :: section";
								}
								
								session.setAttribute("courseCategory", courseCategory);
									
								model.addAttribute("shcssList", studentHistoryService.getStudentHistoryCS2(registerNumberList, 
										courseCode, studentStudySystem, pProgramSpecId, StudyStartYear, curriculumVersion, 
										semesterSubId, courseCategory, courseOption, ccCourseId, classGroupId));
								model.addAttribute("minorList", semesterMasterService.getAdditionalLearningTitleByLearnTypeGroupIdSpecIdAndCourseCode(
										minAllowFlag, "MIN", pProgramGroupId, pProgramSpecId, courseCode, studentStudySystem));
								model.addAttribute("honorList", semesterMasterService.getAdditionalLearningTitleByLearnTypeGroupIdSpecIdAndCourseCode(
										honAllowFlag, "HON", pProgramGroupId, pProgramSpecId, courseCode, studentStudySystem));
								model.addAttribute("courseOptionList",semesterMasterService.getRegistrationCourseOption(
										courseOption, genericCourseType, rgrAllowFlag, audAllowFlag, honAllowFlag, 
										minAllowFlag, adlAllowFlag, csAllowFlag, RPEUEAllowFlag, RUCUEAllowFlag));
								
								//model.addAttribute("courseCatalogModel", courseCatalog);
								if ((crCourseStatus == 1) && genericCourseType.equals("TH") && crGenericCourseType.equals("LO"))
								{
									model.addAttribute("courseCatalogModel", courseCatalog);
									model.addAttribute("courseCatalogModel2", courseCatalog2);
								}
								else if ((crCourseStatus == 1) && genericCourseType.equals("LO") && crGenericCourseType.equals("TH"))
								{
									model.addAttribute("courseCatalogModel", courseCatalog2);
									model.addAttribute("courseCatalogModel2", courseCatalog);
								}
								else
								{
									model.addAttribute("courseCatalogModel", courseCatalog);
								}
																		
								model.addAttribute("regAllowFlag", regAllowFlag);
								model.addAttribute("wlAllowFlag", wlAllowFlag);
								model.addAttribute("epjSlotFlag", epjSlotFlag);
								model.addAttribute("rgrAllowFlag", rgrAllowFlag);
								model.addAttribute("minAllowFlag", minAllowFlag);
								model.addAttribute("honAllowFlag", honAllowFlag);
								model.addAttribute("RPEUEAllowFlag", RPEUEAllowFlag);
								model.addAttribute("csAllowFlag", csAllowFlag);
								model.addAttribute("RUCUEAllowFlag", RUCUEAllowFlag);
								model.addAttribute("page", page);
								model.addAttribute("srhType", searchType);
								model.addAttribute("srhVal", searchVal);
								model.addAttribute("courseOption", courseOption);
								model.addAttribute("registrationOption", registrationOption);						
								model.addAttribute("audAllowFlag", audAllowFlag);
								model.addAttribute("adlAllowFlag", adlAllowFlag);
								model.addAttribute("tlcourseType", courseTypeArr);					
								model.addAttribute("courseTypeDisplay", courseTypeDisplay);
								model.addAttribute("genericCourseTypeDisplay", genericCourseTypeDisplay);
								model.addAttribute("ProgramGroupCode", pProgramGroupCode);
								model.addAttribute("subCourseOption", subCourseOption);
								model.addAttribute("subCourseType", subCourseType);
								model.addAttribute("subCourseDate", subCourseDate);
								model.addAttribute("regularFlag", regularFlag);	
								model.addAttribute("tlCourseCategory", courseCategory);
								model.addAttribute("tlCompCourseList", compCourseList);
								model.addAttribute("crCourseStatus", crCourseStatus);
								model.addAttribute("crSubCourseOption", crSubCourseOption);
								model.addAttribute("crSubCourseType", crSubCourseType);
								model.addAttribute("crSubCourseDate", crSubCourseDate);
																		
								break;  
										
							case 2:
								model.addAttribute("infoMessage", message);									
								urlPage = processRegistrationOption(registrationOption, model, session, 5, page, searchType, 
												searchVal, subCourseOption, request);									
								break;  
						}					
						break;						
						
					default:
						msg = infoMsg;
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						model.addAttribute("courseRegModelList", wishlistRegistrationService.getCourseByRegisterNumberAndClassGroup(
								semesterSubId, classGroupId, registerNumber));
						urlPage = "redirectpage";
						return urlPage;						
				}
				
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		}		
		catch(Exception ex)
		{
			logger.trace(ex.toString());
			
			courseRegistrationReadWriteService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationFormController", 
					"processDeleteCourseRegistration", registerNumber, IpAddress);
			courseRegistrationReadWriteService.updateRegistrationLogLogoutTimeStamp2(IpAddress,registerNumber);
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			
			return urlPage;
		}		
		
		return urlPage;
	}
	
	
	@PostMapping(value = "processRegisterCourse")
	public String processRegisterCourse(String ClassID, String courseId, String courseType, String courseCode, 
							String courseOption, String clashSlot, String epjSlotFlag, 
							@RequestParam(value = "pageSize", required = false) Integer pageSize, 
							@RequestParam(value = "page", required = false) Integer page,
							@RequestParam(value = "searchType", required = false) Integer searchType, 
							@RequestParam(value = "searchVal", required = false) String searchVal,
							@RequestParam(value = "subCourseOption", required = false) String subCourseOption, 
							@RequestParam(value = "subCourseType", required = false) String subCourseType,
							@RequestParam(value = "subCourseDate", required = false) String subCourseDate,
							String[] clArr, Integer crCourseStatus, String crCourseId, String crCourseCode, 
							String crCourseType, String crSubCourseOption, String crSubCourseType, 
							String crSubCourseDate, Model model, HttpSession session, HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String registrationOption = (String) session.getAttribute("registrationOption");
		String IpAddress = (String) session.getAttribute("IpAddress");
				
		Integer updateStatus = 1;
		int allowStatus = 2, chkFlag = 2, compulsoryStatus = 2, componentType = 0;
		String msg = null, infoMsg = "", urlPage = "", message = null, genericCourseType = "", crGenericCourseType = "", 
					equivalanceCourseId = null, equivalanceCourseType = null;
		Date equivalanceExamMonth = null;
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
		
		CourseCatalogModel courseCatalogModel = new CourseCatalogModel();
		CourseCatalogModel courseCatalogModel2 = new CourseCatalogModel();
		List<String> courseTypeList = new ArrayList<String>();
		List<String> regCourseIdList = new ArrayList<String>();
		List<String> regCourseTypeList = new ArrayList<String>();
		List<String> eqvCourseIdList = new ArrayList<String>();
		List<String> eqvCourseTypeList = new ArrayList<String>();
		List<String> eqvCourseDateList = new ArrayList<String>();
		
		logger.trace("\n ClassID: "+ ClassID +" | courseId: "+ courseId 
				+" | courseType: "+ courseType +" | courseCode: "+ courseCode 
				+" | courseOption: "+ courseOption +" | clashSlot: "+ clashSlot
				+" | epjSlotFlag: "+ epjSlotFlag +" | subCourseOption: "+ subCourseOption 
				+" | subCourseType: "+ subCourseType +" | subCourseDate: "+ subCourseDate
				+" | clArr: "+ Arrays.toString(clArr));
		logger.trace("\n crCourseStatus: "+ crCourseStatus +" | crCourseId: "+ crCourseId 
				+" | crCourseCode: "+ crCourseCode +" | crCourseType: "+ crCourseType 
				+" | crSubCourseOption: "+ crSubCourseOption +" | crSubCourseType: "+ crSubCourseType 
				+" | crSubCourseDate: "+ crSubCourseDate);
				
		try
		{
			String authStatus = (String) session.getAttribute("authStatus");
			int authCheckStatus = courseRegCommonFn.validateCourseAuthKey(authStatus, registerNumber, courseId, 1);
			
			logger.trace("authCheckStatus: "+ authCheckStatus +" | registerNumber: "+ registerNumber);
			
			if ((authCheckStatus == 1) && (registerNumber!=null))
			{				
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer StudentGraduateYear = (Integer) session.getAttribute("StudentGraduateYear");
				Integer semesterId  = (Integer) session.getAttribute("SemesterId");
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String pOldRegisterNumber = (String) session.getAttribute("OldRegNo");
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				
				@SuppressWarnings("unchecked")
				List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				Integer compulsoryCourseStatus = (Integer) session.getAttribute("compulsoryCourseStatus");
				
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegistrationReadWriteService.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];		
							
				courseCatalogModel = courseCatalogService.getOne(courseId);
				if (courseCatalogModel != null)
				{
					genericCourseType = courseCatalogModel.getGenericCourseType();
				}
				
				if (crCourseStatus == 1)
				{
					courseCatalogModel2 = courseCatalogService.getOne(crCourseId);
					if (courseCatalogModel2 != null)
					{
						crGenericCourseType = courseCatalogModel2.getGenericCourseType();
					}
				}
				logger.trace("\n courseCatalogModel: "+ courseCatalogModel.toString());
				logger.trace("\n clArr: "+ clArr.length);
																				
				switch (allowStatus)
				{
					case 1:						
						if ((courseCatalogModel != null) && (clArr.length > 0))
						{
							if ((crCourseStatus == 1) && genericCourseType.equals("TH") && crGenericCourseType.equals("LO"))
							{
								regCourseIdList.addAll(Arrays.asList(courseId, crCourseId));
								regCourseTypeList.addAll(Arrays.asList("TH", "LO"));
							}
							else
							{								
								courseTypeList = semesterMasterService.getCourseTypeComponentByGenericType(genericCourseType);
								for (int i = 0; i<clArr.length; i++)
								{
									if (courseTypeList.contains(clArr[i]))
									{
										regCourseIdList.add(courseId);
										regCourseTypeList.add(clArr[i]);
									}
								}
							}
							
							chkFlag = 1;
						}
						else
						{
							msg = "Invalid details.";
						}
					
						if (chkFlag == 1)
						{												
							if ((!subCourseOption.equals("")) && (!subCourseOption.equals(null)))
							{
								switch(courseOption)
								{
									case "RR":
									case "RRCE":
									case "GI":
									case "GICE":
									case "RGCE":
									case "RPCE":
									case "RWCE":										
										if (crCourseStatus == 1)
										{
											eqvCourseIdList.addAll(Arrays.asList(subCourseOption, crSubCourseOption));
											eqvCourseTypeList.addAll(Arrays.asList(subCourseType, crSubCourseType));
											eqvCourseDateList.addAll(Arrays.asList(subCourseDate, crSubCourseDate));
										}
										else
										{
											for (int i=0; i < regCourseIdList.size(); i++)
											{
												eqvCourseIdList.add(subCourseOption);
												eqvCourseTypeList.add(subCourseType);
												eqvCourseDateList.add(subCourseDate);
											}
										}
																				
										break;
										
									case "CS":
										String[] subCrsOptArr = subCourseOption.split("/");
										subCourseOption = subCrsOptArr[0];
										subCourseType = subCrsOptArr[1];
										subCourseDate = subCrsOptArr[2];
										
										for (int i=0; i < regCourseIdList.size(); i++)
										{
											eqvCourseIdList.add(subCourseOption);
											eqvCourseTypeList.add(subCourseType);
											eqvCourseDateList.add(subCourseDate);
										}
										
										break;
										
									default:
										subCourseType = "";
										subCourseDate = "";
										break;
								}
							}
							
							if (regCourseIdList.size() > 0)
							{
								if ((classGroupId!= null) && (courseId !=null) && (registerNumber !=null) 
										&& (semesterSubId !=null) && (courseOption !=null))
								{
									synchronized (this)
									{											
										int index;
										for (index = 0; index < regCourseIdList.size(); index++)
										{
											equivalanceCourseId = null;
											equivalanceCourseType = null;
											equivalanceExamMonth = null;
											
											if (!eqvCourseIdList.isEmpty())
											{
												equivalanceCourseId = eqvCourseIdList.get(index);
												equivalanceCourseType = eqvCourseTypeList.get(index);
												equivalanceExamMonth = format.parse(eqvCourseDateList.get(index));
											}
																						
											courseRegistrationReadWriteService.saveWishlistRegistration(semesterSubId, registerNumber, classGroupId[0], 
													regCourseIdList.get(index), regCourseTypeList.get(index), courseOption, componentType, equivalanceCourseId, 
													equivalanceCourseType, equivalanceExamMonth, registerNumber, IpAddress);
										}
									}
								}
								else 
								{
									msg = "Technical Error";
									session.setAttribute("info", msg);
									model.addAttribute("flag", 2);
									urlPage = "redirectpage";
								}
							}
							
							msg = "Registered Successfully.";
						}
										
						session.setAttribute("authStatus", "NONE");
													
						
						if (compulsoryCourseStatus == 1)
						{
							compulsoryStatus = courseRegCommonFn.compulsoryCourseCheck(programGroupId, studyStartYear, 
													StudentGraduateYear, semesterId, semesterSubId, registerNumber, 
													classGroupId, classType, ProgramSpecCode, programSpecId, 
													ProgramGroupCode, pOldRegisterNumber, compCourseList, 
													costCentreCode);
							session.setAttribute("compulsoryCourseStatus", compulsoryStatus);
						}
						
						if (compulsoryStatus == 1)
						{
							getCompulsoryCourseList(registrationOption, pageSize, page, searchType, searchVal, 
									subCourseOption, session, model, compCourseList);
							model.addAttribute("info", msg);
							urlPage = "mainpages/CompulsoryCourseList :: section";
						}
						else
						{
							if (registrationOption.equals("COMP"))
							{
								session.removeAttribute("registrationOption");
								model.addAttribute("studySystem", session.getAttribute("StudySystem"));
								model.addAttribute("regularFlag", session.getAttribute("regularFlag"));
								model.addAttribute("registrationMethod", session.getAttribute("registrationMethod"));
								model.addAttribute("showFlag", 0);
								model.addAttribute("info", msg);
								urlPage = "mainpages/RegistrationOptionList :: section";
							}
							else
							{
								callCourseRegistrationTypes(registrationOption, pageSize, page, searchType, searchVal, session, model);
								model.addAttribute("info", msg);
								model.addAttribute("courseRegModelList", wishlistRegistrationService.getCourseByRegisterNumberAndClassGroup(
										semesterSubId, classGroupId, registerNumber));
								urlPage = "mainpages/CourseList :: section";
							}
						}
						break;
						
					default:
						msg = infoMsg;
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						urlPage = "redirectpage";
						return urlPage;
				}
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		}
		catch(Exception ex)
		{
			logger.trace(ex);
			logger.trace("Regno: "+ registerNumber +" | Message: "+ message 
					+ " | msg: " + msg +" | registrationOption: " + registrationOption);
						
			courseRegistrationReadWriteService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationDeleteController", 
					"processDeleteCourseRegistration", registerNumber, IpAddress);
			courseRegistrationReadWriteService.updateRegistrationLogLogoutTimeStamp2(IpAddress,registerNumber);
			session.setAttribute("authStatus", "NONE");
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			
			return urlPage;
		}
		model.addAttribute("infoMessage", message);
		
		return urlPage;		
	}
	

	@PostMapping("processSearch")
	public String processSearch(Model model, HttpSession session, 
						@RequestParam(value = "pageSize", required = false) Integer pageSize,
						@RequestParam(value = "page", required = false) Integer page, 
						@RequestParam(value = "searchType", required = false) Integer searchType,
						@RequestParam(value = "searchVal", required = false) String searchVal, 
						@RequestParam(value = "subCourseOption", required = false) String subCourseOption, 
						HttpServletRequest request) 
	{	
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		
		String msg = null, infoMsg = "", urlPage = "";
		Integer updateStatus = 1;
		int allowStatus = 2;
						
		try 
		{
			if (registerNumber != null)
			{	
				String registrationOption = (String) session.getAttribute("registrationOption");
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegistrationReadWriteService.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
					
				switch (allowStatus)
				{
					case 1:
						callCourseRegistrationTypes(registrationOption, pageSize, page, searchType, searchVal, 
								session, model);
						model.addAttribute("registrationOption", registrationOption);
						model.addAttribute("searchFlag", 1);
						urlPage = "mainpages/CourseList::section";						
						break;
					
					default:
						msg = infoMsg;
						session.setAttribute("info", msg);
						model.addAttribute("flag", 2);
						urlPage = "redirectpage";
						return urlPage;						
				}					
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		} 
		catch (Exception ex) 
		{
			logger.trace(ex);
			
			courseRegistrationReadWriteService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationDeleteController", 
					"processDeleteCourseRegistration", registerNumber, IpAddress);
			courseRegistrationReadWriteService.updateRegistrationLogLogoutTimeStamp2(IpAddress,registerNumber);
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			
			return urlPage;
		}
		
		return urlPage; 
	}
	
	public int getCompulsoryCourseList(String registrationOption, Integer pageSize, Integer page, Integer searchType,
					String searchVal, String subCourseOption, HttpSession session, Model model, List<String> courseCode)
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");		
		Pager pager = null;
		int evalPageSize = INITIAL_PAGE_SIZE;
		int evalPage = INITIAL_PAGE;
		evalPageSize = pageSize == null ? INITIAL_PAGE_SIZE : pageSize;
		evalPage = (page == null || page < 1) ? INITIAL_PAGE : page - 1;
		int pageSerialNo = evalPageSize * evalPage;
		int srhType = (searchType == null) ? 0 : searchType;
		String srhVal = (searchVal == null) ? "NONE" : searchVal;
				
		try
		{
			if (registerNumber != null)
			{
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				Integer ProgramGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String[] courseSystem = (String[]) session.getAttribute("StudySystem");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String ProgramGroupCode = (String) session.getAttribute("ProgramGroupCode");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				String costCentreCode = (String) session.getAttribute("costCentreCode");
				
				@SuppressWarnings("unchecked")
				List<Integer> egbGroupId = (List<Integer>) session.getAttribute("EligibleProgramLs");
				Page<CourseCatalogModel> courseCatalogModelPageList = null;
								
				if (registrationOption != null) 
				{
					session.setAttribute("registrationOption", registrationOption);
				} 
				else 
				{
					registrationOption = (String) session.getAttribute("registrationOption");
				}
				
				List<String> courseRegModelList = wishlistRegistrationService.getCourseByRegisterNumberAndClassGroup(
														semesterSubId, classGroupId, registerNumber);
								
				if (srhType == 0)
				{
					courseCatalogModelPageList = courseCatalogService.getCompulsoryCoursePagination(CAMPUSCODE, courseSystem, 
														egbGroupId, ProgramGroupId.toString(), semesterSubId, classGroupId, 
														classType, courseCode, ProgramGroupCode, ProgramSpecCode, costCentreCode, 
														PageRequest.of(evalPage, evalPageSize));
					pager = new Pager(courseCatalogModelPageList.getTotalPages(), courseCatalogModelPageList.getNumber(), 
									BUTTONS_TO_SHOW);
				}
				
				if (courseCatalogModelPageList != null)
				{
					model.addAttribute("courseCatalogModelPageList", courseCatalogModelPageList);
				}
				
				model.addAttribute("registrationOption", registrationOption);
				model.addAttribute("courseRegModelList", courseRegModelList);
				model.addAttribute("pageSlno", pageSerialNo);
				model.addAttribute("selectedPageSize", evalPageSize);
				model.addAttribute("pageSizes", PAGE_SIZES);
				model.addAttribute("srhType", srhType);
				model.addAttribute("srhVal", srhVal);
				model.addAttribute("pager", pager);
				model.addAttribute("page", page);
			}
			
		}
		catch(Exception ex)
		{
			logger.trace(ex);
		}
		
		return 1;
	}
	
	@PostMapping(value="processPageNumbers")
	public String processPageNumbers(Model model, HttpSession session, HttpServletRequest request, 
						@RequestParam(value="pageSize", required=false) Integer pageSize,
						@RequestParam(value="page", required=false) Integer page, 
						@RequestParam(value="searchType", required=false) Integer searchType, 
						@RequestParam(value="searchVal", required=false) String searchVal, 
						@RequestParam(value="totalPage", required=false) Integer totalPage, 
						@RequestParam(value="processType", required=false) Integer processType)
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		String urlPage = "";
		
		logger.trace("registerNumber: "+ registerNumber +" | IpAddress: "+ IpAddress);
		logger.trace("pageSize: "+ pageSize +" | page: "+ page +" | searchType: "+ searchType 
			+" | searchVal: "+ searchVal +" | totalPage: "+ totalPage +" | processType: "+ processType);
		
		try
		{
			if (registerNumber != null)
			{				
				Pager pager = null;		
				int evalPageSize = INITIAL_PAGE_SIZE;
				int evalPage = INITIAL_PAGE;
				evalPageSize = pageSize == null ? INITIAL_PAGE_SIZE : pageSize;
				evalPage = (page == null || page < 1) ? INITIAL_PAGE : page - 1;
				int pageSerialNo = evalPageSize * evalPage;
				int srhType = (searchType == null) ? 0 : searchType;
				String srhVal = (searchVal == null) ? "NONE" : searchVal;
				
				int pageNumber = evalPage;
				
				if (pageNumber <= 0)
				{
					pageNumber = 0;
				}
				else if ((int)pageNumber >= (int)totalPage)
				{
					pageNumber = totalPage - 1;
				}
				
				pager = new Pager(totalPage, pageNumber, BUTTONS_TO_SHOW);
				
				model.addAttribute("tlTotalPage", totalPage);
				model.addAttribute("tlPageNumber", pageNumber);
				model.addAttribute("pageSlno", pageSerialNo);
				model.addAttribute("selectedPageSize", evalPageSize);
				model.addAttribute("pageSizes", PAGE_SIZES);
				model.addAttribute("srhType", srhType);
				model.addAttribute("srhVal", srhVal);
				model.addAttribute("pager", pager);
				model.addAttribute("page", page);
				
				logger.trace("totalPage: "+ totalPage +" | pageNumber: "+ pageNumber);
				logger.trace("pageSerialNo: "+ pageSerialNo +" | evalPageSize: "+ evalPageSize 
						+" | srhType: "+ srhType +" | srhVal: "+ srhVal +" | pager: "+ pager 
						+" | page: "+ page);
				
				if (processType == 1)
				{
					urlPage = "mainpages/CourseList :: pageNoFrag";
				}
				else if (processType == 2)
				{
					urlPage = "mainpages/CourseList :: pageNoFrag2";
				}
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		}
		catch(Exception e)
		{
			logger.trace(e);
			
			courseRegistrationReadWriteService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationDeleteController", 
					"processDeleteCourseRegistration", registerNumber, IpAddress);
			courseRegistrationReadWriteService.updateRegistrationLogLogoutTimeStamp2(IpAddress,registerNumber);
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			
			return urlPage;			
		}		

		return urlPage;
	}
	
	//Calling Slot Information When Required.
	public void callSlotInformation(Model model, String semesterSubId, String registerNumber, List<CourseAllocationModel> courseAllocationList)
	{
		List<Object[]> registeredObjectList = new ArrayList<Object[]>();
		Map<String, List<SlotTimeMasterModel>> slotTimeMapList = new HashMap<String, List<SlotTimeMasterModel>>();
		
		registeredObjectList = courseRegistrationService.getRegistrationAndWaitingSlotDetail(semesterSubId, registerNumber);
		slotTimeMapList = semesterMasterService.getSlotTimeMasterCommonTimeSlotBySemesterSubIdAsMap(Arrays.asList(semesterSubId));
		
		model.addAttribute("tlInfoMapList", courseRegCommonFn.getSlotInfo(registeredObjectList, courseAllocationList, slotTimeMapList));
	}
}
