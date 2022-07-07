package org.vtop.CourseRegistration.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.vtop.CourseRegistration.model.CourseCatalogModel;
import org.vtop.CourseRegistration.model.WishlistRegistrationModel;
import org.vtop.CourseRegistration.service.CourseCatalogService;
import org.vtop.CourseRegistration.service.CourseRegistrationCommonFunction;
import org.vtop.CourseRegistration.service.CourseRegistrationReadWriteService;
import org.vtop.CourseRegistration.service.ProgrammeSpecializationCurriculumDetailService;
import org.vtop.CourseRegistration.service.WishlistRegistrationService;


@Controller
public class CourseRegistrationDeleteController 
{
	@Autowired private CourseRegistrationCommonFunction courseRegCommonFn;
	@Autowired private WishlistRegistrationService wishlistRegistrationService;
	@Autowired private CourseRegistrationReadWriteService courseRegistrationReadWriteService;
	@Autowired private CourseCatalogService courseCatalogService;
	@Autowired private ProgrammeSpecializationCurriculumDetailService programmeSpecializationCurriculumDetailService;
	
	private static final Logger logger = LogManager.getLogger(CourseRegistrationDeleteController.class);
	private static final String RegErrorMethod = "FS-2223-WL";
	private static final List<String> crCourseOption = new ArrayList<String>(Arrays.asList("RGR","RGCE", 
																	"RGP","RGW","RPCE","RWCE","RR"));
	
	
	@PostMapping("processDeleteCourseRegistration")
	public String processDeleteCourseRegistration(String courseId, Model model, HttpSession session, 
						HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		
		Integer allowStatus = 0, updateStatus = 1;
		int delStatusFlag = 2, deleteAllowStatus = 0, crCourseStatus = 2;
		String urlPage = "", msg = null, infoMsg = "", courseAuthStatus = "", deleteMessage="", ccCourseSystem = "", 
					crCourseId = "", crCourseCode = "";
		String[] regStatusArr = new String[5];
		
		CourseCatalogModel courseCatalog = new CourseCatalogModel();
		List<WishlistRegistrationModel> wishlistRegistrationModelList = new ArrayList<>();
		
		try
		{	
			if (registerNumber != null)
			{	
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer programGroupId = (Integer) session.getAttribute("ProgramGroupId");
				String ProgramSpecCode = (String) session.getAttribute("ProgramSpecCode");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				String programGroupCode = (String) session.getAttribute("ProgramGroupCode");
				String programGroupMode = (String) session.getAttribute("programGroupMode");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
				int otpStatus = (int) session.getAttribute("otpStatus");
				
				@SuppressWarnings("unchecked")
				List<String> compCourseList = (List<String>) session.getAttribute("compulsoryCourseList");			
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegistrationReadWriteService.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				courseCatalog = courseCatalogService.getOne(courseId);
				if (courseCatalog != null)
				{
					ccCourseSystem = courseCatalog.getCourseSystem();
										
					if ((courseCatalog.getCorequisite() != null) && (!courseCatalog.getCorequisite().equals("")) 
							&& (!courseCatalog.getCorequisite().equals("NONE")) && (!courseCatalog.getCorequisite().equals("NIL")))
					{
						crCourseCode = courseCatalog.getCorequisite().trim();
					}
				}
				
				if ((!ccCourseSystem.equals("NONFFCS")) && (!ccCourseSystem.equals("FFCS")) 
						&& (!ccCourseSystem.equals("CAL")) && (!crCourseCode.equals("")))
				{						
					wishlistRegistrationModelList = wishlistRegistrationService.getByRegisterNumberCourseCode(semesterSubId, 
														classGroupId, registerNumber, crCourseCode);
					if (!wishlistRegistrationModelList.isEmpty())
					{
						for (WishlistRegistrationModel e : wishlistRegistrationModelList)
						{
							if (crCourseOption.contains(e.getCourseOptionCode()))
							{
								crCourseId = e.getCourseCatalogModel().getCourseId();
								crCourseStatus = 1;
							}
														
							break;
						}
					}
				}
				
				switch(allowStatus)
				{
					case 1:				
						regStatusArr = courseRegCommonFn.checkRegistrationDeleteCondition(semesterSubId, registerNumber, 
											courseId, programGroupId, programGroupCode, programGroupMode, ProgramSpecCode, 
											programSpecId, studyStartYear, curriculumVersion, compCourseList, classGroupId).split("\\|");
						delStatusFlag = Integer.parseInt(regStatusArr[0]);
						deleteMessage = regStatusArr[1];
						courseAuthStatus = regStatusArr[2];
						
						if ((delStatusFlag == 1) && (crCourseStatus == 1))
						{
							regStatusArr = courseRegCommonFn.checkRegistrationDeleteCondition(semesterSubId, registerNumber, 
												crCourseId, programGroupId, programGroupCode, programGroupMode, ProgramSpecCode, 
												programSpecId, studyStartYear, curriculumVersion, compCourseList, classGroupId).split("\\|");
							delStatusFlag = Integer.parseInt(regStatusArr[0]);
							deleteMessage = regStatusArr[1];
						}
						
						if (otpStatus == 1)
						{
							deleteAllowStatus = 1;
						}
						else if (otpStatus == 2)
						{
							courseAuthStatus = courseRegCommonFn.generateCourseAuthKey(registerNumber, courseId, delStatusFlag, 2);
						}
						
						session.setAttribute("authStatus", courseAuthStatus);
						
						if (delStatusFlag == 1)
						{	
							wishlistRegistrationModelList.clear();
							wishlistRegistrationModelList.addAll(wishlistRegistrationService.getByRegisterNumberCourseId(semesterSubId, 
									classGroupId, registerNumber, courseId));
							if (crCourseStatus == 1)
							{
								wishlistRegistrationModelList.addAll(wishlistRegistrationService.getByRegisterNumberCourseId(semesterSubId, 
										classGroupId, registerNumber, crCourseId));
								session.setAttribute("crCourseId", crCourseId);
							}
							
							model.addAttribute("courseRegistrationModel", wishlistRegistrationModelList);
							model.addAttribute("courseId", courseId);
							model.addAttribute("msg", deleteMessage);
							model.addAttribute("tlDeleteAllowStatus", deleteAllowStatus);
							
							urlPage = "mainpages/DeleteConfirmation :: section";
						}
						else
						{
							model.addAttribute("courseRegistrationModel", wishlistRegistrationService.getByRegisterNumberAsObject(semesterSubId, 
									classGroupId, registerNumber));
							model.addAttribute("blockedCourse", wishlistRegistrationService.getBlockedCourseIdByRegisterNumberForDelete(
									semesterSubId, classGroupId, registerNumber));
							model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
									getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, curriculumVersion));
							model.addAttribute("showFlag", 0);
							model.addAttribute("infoMessage", deleteMessage);
	
							urlPage = "mainpages/DeleteCourse :: section";
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

	@PostMapping("processDeleteCourseRegistrationOTP")
	public String processDeleteCourseRegistrationOTP(String courseId, Model model, HttpSession session, 
						HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		
		Integer allowStatus = 0, updateStatus = 1;
		int deleteAllowStatus = 0, redirectFlag = 2, statusFlag = 2;
		String urlPage = "", msg = null, infoMsg = "", courseAuthStatus = "", deleteMessage = "", courseCode = "";
		String[] validateStatusArr = new String[]{};
		List<WishlistRegistrationModel> wishlistRegistrationModelList = new ArrayList<>();
		
		try
		{	
			if (registerNumber != null)
			{	
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				String semesterDesc = (String) session.getAttribute("SemesterDesc");
				String semesterShortDesc = (String) session.getAttribute("SemesterShortDesc");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String studentEMailId = (String) session.getAttribute("studentEMailId");
				int otpStatus = (int) session.getAttribute("otpStatus");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
				String crCourseId = (String) session.getAttribute("crCourseId");
							
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegistrationReadWriteService.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				switch(allowStatus)
				{
					case 1:
						String authStatus = (String) session.getAttribute("authStatus");
						int authCheckStatus = courseRegCommonFn.validateCourseAuthKey(authStatus, registerNumber, courseId, 1);
						
						if ((authCheckStatus == 1) && (otpStatus == 1))
						{	
							CourseCatalogModel courseCatalogModel = courseCatalogService.getOne(courseId);
							if (courseCatalogModel != null)
							{
								courseCode = courseCatalogModel.getCode();
							}
							
							validateStatusArr = courseRegistrationReadWriteService.validateCourseAndSendOTP(semesterSubId, semesterDesc, 
													semesterShortDesc, registerNumber, courseId, courseCode, studentEMailId, IpAddress, 
													"DELETE").split("\\|");
							statusFlag = Integer.parseInt(validateStatusArr[0]);
							courseAuthStatus = validateStatusArr[1];
							deleteMessage = validateStatusArr[3];
							logger.trace("statusFlag: "+ statusFlag +" | courseAuthStatus: "+ courseAuthStatus 
									+" | deleteMessage: "+ deleteMessage); 
							
							if (statusFlag == 1)
							{
								deleteAllowStatus = 2;
								redirectFlag = 1;
							}
						}
						else
						{
							deleteMessage = "Invalid course...!";
						}
						
				
						if (redirectFlag == 1)
						{
							wishlistRegistrationModelList.addAll(wishlistRegistrationService.getByRegisterNumberCourseId(semesterSubId, 
									classGroupId, registerNumber, courseId));
							if ((crCourseId != null) && (!crCourseId.equals("")))
							{
								wishlistRegistrationModelList.addAll(wishlistRegistrationService.getByRegisterNumberCourseId(semesterSubId, 
										classGroupId, registerNumber, crCourseId));
							}
							
							session.setAttribute("authStatus", courseAuthStatus);
							
							model.addAttribute("courseRegistrationModel", wishlistRegistrationModelList);
							model.addAttribute("courseId", courseId);
							model.addAttribute("msg", deleteMessage);
							model.addAttribute("tlDeleteAllowStatus", deleteAllowStatus);
							
							urlPage = "mainpages/DeleteConfirmation :: section";
						}
						else
						{							
							model.addAttribute("courseRegistrationModel", wishlistRegistrationService.getByRegisterNumberAsObject(semesterSubId, 
									classGroupId, registerNumber));
							model.addAttribute("blockedCourse", wishlistRegistrationService.getBlockedCourseIdByRegisterNumberForDelete(
									semesterSubId, classGroupId, registerNumber));
							model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
									getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, curriculumVersion));
							model.addAttribute("showFlag", 0);
							model.addAttribute("infoMessage", deleteMessage);
	
							urlPage = "mainpages/DeleteCourse :: section";
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
	
	@PostMapping("processDeleteConfirmationCourseRegistration")
	public String processDeleteConfirmationCourseRegistration(String courseId, Model model, HttpSession session, 
						HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		
		int redirectFlag = 2, statusFlag = 2;
		Integer allowStatus = 2, updateStatus = 1;
		String urlPage = "", courseCode = "", mailOTP = "", msg = null, message = null, infoMsg = "";
		String[] validateStatusArr = new String[]{};
				
		try
		{	
			if (registerNumber != null)
			{
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				String semesterDesc = (String) session.getAttribute("SemesterDesc");
				String semesterShortDesc = (String) session.getAttribute("SemesterShortDesc");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");	
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
				int otpStatus = (int) session.getAttribute("otpStatus");
				String crCourseId = (String) session.getAttribute("crCourseId");
						
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
								
				String returnVal = courseRegistrationReadWriteService.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				mailOTP = request.getParameter("mailOTP");
				if ((mailOTP != null) && (!mailOTP.equals("")))
				{
					mailOTP = mailOTP.trim();
				}
				else
				{
					mailOTP = "NONE";
				}
								
				String authStatus = (String) session.getAttribute("authStatus");
				int authCheckStatus = courseRegCommonFn.validateCourseAuthKey(authStatus, registerNumber, courseId, 2);
				
				switch(allowStatus)
				{
					case 1:
						if(authCheckStatus == 1)
						{
							if (otpStatus == 1)
							{								
								CourseCatalogModel courseCatalogModel = courseCatalogService.getOne(courseId);
								if (courseCatalogModel != null)
								{
									courseCode = courseCatalogModel.getCode();
								}
								
								validateStatusArr = courseRegistrationReadWriteService.validateCourseAndOTP(semesterSubId, 
														semesterDesc, semesterShortDesc, registerNumber, courseId, 
														courseCode, mailOTP, IpAddress, "DELETE").split("\\|");
								statusFlag = Integer.parseInt(validateStatusArr[0]);
								redirectFlag = Integer.parseInt(validateStatusArr[1]);
																
								if(validateStatusArr[2].toString().equals("SUCCESS"))
								{
									msg = "Registered Course(s) Successfully Deleted.";
								}
								else
								{
									message = validateStatusArr[2].toString();
								}
							}
							else if (otpStatus == 2)
							{
								statusFlag = 1;
								redirectFlag = 2;
							}
							logger.trace("statusFlag: "+ statusFlag +" | redirectFlag: "+ redirectFlag 
									+" | message: "+ message); 
										
							if (statusFlag == 1)
							{
								courseRegistrationReadWriteService.deleteWishlistRegistrationByRegisterNumberAndCourseId(semesterSubId, 
										classGroupId, registerNumber, courseId, registerNumber, IpAddress);
								
								if ((crCourseId != null) && (!crCourseId.equals("")))
								{
									courseRegistrationReadWriteService.deleteWishlistRegistrationByRegisterNumberAndCourseId(semesterSubId, 
											classGroupId, registerNumber, crCourseId, registerNumber, IpAddress);
								}
								
								msg = "Registered Course(s) Successfully Deleted.";
							}
						}
						else
						{
							message = "Not a valid course to delete.";
						}	
												
						if (redirectFlag == 1)
						{		
							model.addAttribute("courseRegistrationModel", wishlistRegistrationService.getByRegisterNumberCourseId(semesterSubId, 
									classGroupId, registerNumber, courseId));
							model.addAttribute("courseId", courseId);
							model.addAttribute("info", msg);
							model.addAttribute("infoMessage", message);
							model.addAttribute("tlDeleteAllowStatus", 2);
							
							urlPage = "mainpages/DeleteConfirmation :: section";
						}
						else
						{								
							model.addAttribute("courseRegistrationModel", wishlistRegistrationService.getByRegisterNumberAsObject(semesterSubId, 
									classGroupId, registerNumber));
							model.addAttribute("blockedCourse", wishlistRegistrationService.getBlockedCourseIdByRegisterNumberForDelete(
									semesterSubId, classGroupId, registerNumber));
							model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
									getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, curriculumVersion));
							model.addAttribute("info", msg);
							model.addAttribute("infoMessage", message);
							
							urlPage = "mainpages/DeleteCourse::section";
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

			
	@PostMapping("processDeleteConfirmationCourseRegistrationRirect")
	public String processDeleteConfirmationCourseRegistrationRirect(Model model, HttpSession session, 
						HttpServletRequest request) 
	{		
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		
		String msg = null, infoMsg = "", urlPage = "";
		Integer allowStatus = 2, updateStatus = 1;
							
		try
		{	
			if (registerNumber != null)
			{
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
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
								
				switch(allowStatus)
				{
					case 1:
						model.addAttribute("courseRegistrationModel", wishlistRegistrationService.getByRegisterNumberAsObject(semesterSubId, 
								classGroupId, registerNumber));
						model.addAttribute("blockedCourse", wishlistRegistrationService.getBlockedCourseIdByRegisterNumberForDelete(
								semesterSubId, classGroupId, registerNumber));
						model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
								getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, curriculumVersion));
						urlPage = "mainpages/DeleteCourse::section";
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
		
		return urlPage;			
	}
	

	@PostMapping("deleteRegisteredCourse")
	public String deleteRegisteredCourse(Model model, HttpSession session, HttpServletRequest request) 
	{	
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		
		String msg = null, infoMsg = "", urlPage = "";
		Integer allowStatus = 2, updateStatus = 1;
		
		try
		{	
			if (registerNumber != null)
			{
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
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
				
				switch(allowStatus)
				{
					case 1:
						model.addAttribute("courseRegistrationModel", wishlistRegistrationService.getByRegisterNumberAsObject(semesterSubId, 
								classGroupId, registerNumber));
						model.addAttribute("blockedCourse", wishlistRegistrationService.getBlockedCourseIdByRegisterNumberForDelete(
								semesterSubId, classGroupId, registerNumber));
						model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
								getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, curriculumVersion));
						model.addAttribute("showFlag", 0);
						
						urlPage = "mainpages/DeleteCourse::section";
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
		
		return urlPage;
	}
}
