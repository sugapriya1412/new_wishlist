package org.vtop.CourseRegistration.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.vtop.CourseRegistration.NetAssist;
import org.vtop.CourseRegistration.model.EmployeeProfile;
import org.vtop.CourseRegistration.model.PatternTimeMasterModel;
import org.vtop.CourseRegistration.model.SlotTimeMasterModel;
import org.vtop.CourseRegistration.model.StudentsLoginDetailsModel;
import org.vtop.CourseRegistration.service.CourseRegistrationCommonFunction;
import org.vtop.CourseRegistration.service.CourseRegistrationReadWriteService;
import org.vtop.CourseRegistration.service.CourseRegistrationService;
import org.vtop.CourseRegistration.service.ProgrammeSpecializationCurriculumCreditService;
import org.vtop.CourseRegistration.service.ProgrammeSpecializationCurriculumDetailService;
import org.vtop.CourseRegistration.service.RegistrationLogService;
import org.vtop.CourseRegistration.service.SemesterMasterService;
import org.vtop.CourseRegistration.service.WishlistRegistrationService;

import eu.bitwalker.useragentutils.UserAgent;
import eu.bitwalker.useragentutils.Version;


@Controller
public class CourseRegistrationPageController 
{
	@Autowired private CourseRegistrationService courseRegistrationService;
	@Autowired private RegistrationLogService registrationLogService;
	@Autowired private CourseRegistrationCommonFunction courseRegCommonFn;
	@Autowired private ProgrammeSpecializationCurriculumCreditService programmeSpecializationCurriculumCreditService;
	@Autowired private ProgrammeSpecializationCurriculumDetailService programmeSpecializationCurriculumDetailService;
	@Autowired private SemesterMasterService semesterMasterService;
	@Autowired private CourseRegistrationReadWriteService courseRegistrationReadWriteService;
	@Autowired private WishlistRegistrationService wishlistRegistrationService;
	
	private static final Logger logger = LogManager.getLogger(CourseRegistrationPageController.class);
	private static final String RegErrorMethod = "FS-2223-WL";
	

	@RequestMapping(value = "SessionTimedOut", method = { RequestMethod.POST, RequestMethod.GET })
	public String sessionError(@CookieValue(value = "RegisterNumber") String registerNumber, Model model, 
						HttpServletRequest request, HttpServletResponse response, HttpSession session) 
						throws ServletException, IOException 
	{
		String page = "";		
		Cookie[] cookies = request.getCookies();
		
		if (cookies!=null)
		{
			for (Cookie cookie : cookies) 
			{
				if(cookie.getName().equals(registerNumber))
				{
					if (registrationLogService.isExist(registerNumber)) 
					{
						courseRegistrationReadWriteService.updateRegistrationLogLogoutTimeStamp2(request.getRemoteAddr(), registerNumber);
						
						cookie = new Cookie("RegisterNumber", null);
						cookie.setMaxAge(0);
						cookie.setSecure(true);
						cookie.setHttpOnly(true);
						response.addCookie(cookie);
						request.getSession().invalidate();				
					}
				}				
				
				model.addAttribute("message", "Session Expired");
				model.addAttribute("error", "Try Logout and Log-in");
				model.addAttribute("errno", 3);
				page = "CustomErrorPage";
			}			
		}
		else
		{
			courseRegCommonFn.callCaptcha(request,response,session,model);			
			model.addAttribute("flag", 2);			
			page = "redirectpage";							
		}	
		
		return page;
	}

	@RequestMapping(value= "/", method = {RequestMethod.GET, RequestMethod.POST})
	public String home(HttpServletRequest httpServletRequest, Model model, HttpSession session, 
							HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String userAgent = httpServletRequest.getHeader("user-agent");
		UserAgent ua = UserAgent.parseUserAgentString(userAgent);
		Version browserVersion = ua.getBrowserVersion();
		String browserName = ua.getBrowser().toString();
		String userSessionId = null;
		
		String currentDateTimeStr;	
		
		Date currentDateTime = new Date();
		
		currentDateTimeStr = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(currentDateTime);
		
		model.addAttribute("CurrentDateTime", currentDateTimeStr);
		
		session.setAttribute("baseURL", NetAssist.getBaseURL(httpServletRequest));		

		int majVersion = Integer.parseInt(browserVersion.getMajorVersion());
		
		if (browserName.equalsIgnoreCase("Firefox") && majVersion < 50) 
		{
			model.addAttribute("message", "Outdated Web Browser Error!");
			model.addAttribute("error", "Kindly Update Your Browser. We recommend to use Mozilla Firefox or Google Chorme for better experience.");
		} 
		else if (browserName.equalsIgnoreCase("Chrome") && majVersion < 50) 
		{
			model.addAttribute("message", "Outdated Web Browser Error!");
			model.addAttribute("error", "Kindly Update Your Browser. We recommend to use Mozilla Firefox or Google Chorme for better experience.");
			return "ErrorPage";
		}
		else if (browserName.equalsIgnoreCase("EDGE14") && majVersion == 14) 
		{
			model.addAttribute("message", "Outdated Web Browser Error!");
			model.addAttribute("error", "Kindly Update Your Borwser. We recommend to use Mozilla Firefox or Google Chorme for better experience.");
			return "ErrorPage";
		} 
		else if (browserName.equalsIgnoreCase("OPERA") && majVersion < 40) 
		{
			model.addAttribute("message", "Outdated Web Browser Error!");
			model.addAttribute("error", "Kindly Update Your Borwser. We recommend to use Mozilla Firefox or Google Chorme for better experience.");
			return "ErrorPage";
		} 
		else if (browserName.contains("IE")) 
		{
			model.addAttribute("message", "Outdated Web Browser Error!");
			model.addAttribute("error", "Kindly Update Your Borwser. We recommend to use Mozilla Firefox or Google Chorme for better experience.");
			return "ErrorPage";
		}

		userSessionId = (String) session.getAttribute("userSessionId");

		if (userSessionId == null) 
		{
			session.setAttribute("userSessionId", session.getId());
		}
		
		courseRegCommonFn.callCaptcha(request, response, session, model);
		session.setAttribute("CAPTCHA", session.getAttribute("CAPTCHA"));
		
		return "StudentLogin";
	}
		
	@RequestMapping("/login/error")
	public String loginError(Model model, HttpServletRequest request, HttpServletResponse response,
						@RequestParam(value = "error", required = false) String error)
	{
	    String file = "StudentLogin";
	    HttpSession session = request.getSession(false);
	    String errMsg = "";
	    
	    try
	    {
	    	AuthenticationException exp = (AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	    	if (exp != null)
	    	{
	    		errMsg = exp.getMessage();
	    	}
	    	
	    	courseRegCommonFn.callCaptcha(request, response, session, model);
	    }
	    catch (Exception x)
	    {
	    	logger.trace(x);
	    	errMsg = x.getMessage();
	    }
		model.addAttribute("CurrentDateTime", new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(new Date()));
	    model.addAttribute("info", errMsg);

	    return file;
	}
	
	@PostMapping("viewStudentLogin1")
	public String viewStudentLogin1(Model model, HttpServletRequest request, HttpSession session, 
						HttpServletResponse response) throws ServletException, IOException 
	{
		session.setAttribute("baseURL", NetAssist.getBaseURL(request));
		String userSessionId = (String) session.getAttribute("userSessionId");

		if (userSessionId == null) 
		{
			session.setAttribute("userSessionId", session.getId());
		}
		
		courseRegCommonFn.callCaptcha(request, response, session, model);
		session.setAttribute("CAPTCHA", session.getAttribute("CAPTCHA"));
		
		return "StudentLogin::test";
	}	

	@RequestMapping(value = "ServerLimit", method = { RequestMethod.POST, RequestMethod.GET })
	public String serverLimit(Model model, HttpSession session, HttpServletRequest request) throws ServletException 
	{
		String page = "CustomErrorPage";
		String baseURL = NetAssist.getBaseURL(request);
		logger.trace("BaseUrl - " + baseURL);
		
		request.getSession().invalidate();
		model.addAttribute("message", "");
		model.addAttribute("error", " Please Note: Try one of the following Servers <br/><br/>");
		model.addAttribute("errno", 99);

		return page;
	}

	@RequestMapping(value = "AlreadyLogin", method = { RequestMethod.POST, RequestMethod.GET })
	public String AlreadyLogin(Model model,HttpSession session, HttpServletRequest request, 
						HttpServletResponse response) throws ServletException 
	{
		String page = "CustomErrorPage";

		Cookie cookie = new Cookie("RegisterNumber", null);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
		request.getSession().invalidate();
		model.addAttribute("message", "Multi-Tab Access");
		model.addAttribute("error", "Multiple Tabs Access prevented !!!");
		model.addAttribute("errno", 6);
		
		return page;
	}
	
	@GetMapping("signOut")
	public String signOut(Model model,HttpServletRequest request) 
	{
		model.addAttribute("message", " V TOP ");
		model.addAttribute("error", " Thank you For Using V TOP Course Registration Portal .");
		request.getSession().invalidate();	
		
		return "CustomErrorPage";
	}
	
	@GetMapping("noscript")
	public String noscript(Model model) 
	{
		model.addAttribute("message", "JavaScript Error");
		model.addAttribute("error", "Kindly Enable JavaScript in Your Browser to Access V-TOP.");
		
		return "ErrorPage";
	}

	@RequestMapping(value = "processLogout", method = { RequestMethod.POST, RequestMethod.GET })
	public String doLogout(HttpSession session, HttpServletRequest request, HttpServletResponse response, 
						Model model) throws ServletException, IOException 
	{
		String page = "", info = null;
		
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress=(String) session.getAttribute("IpAddress");
		
		try 
		{
			if ((registerNumber != null) && (registrationLogService.isExist(registerNumber)))
			{
				info = (String) session.getAttribute("info");
				
				courseRegistrationReadWriteService.updateRegistrationLogLogoutTimeStamp2(IpAddress,registerNumber);
				model.addAttribute("flag", 4);			
				page = "redirectpage";
				//page = "redirect:/logout";
			}
			else
			{
				model.addAttribute("flag", 4);			
				page = "redirectpage";
			}
			
			model.addAttribute("info", info);
			Cookie cookie = new Cookie("RegisterNumber", null);
			cookie.setMaxAge(0);
			response.addCookie(cookie);
			request.getSession().invalidate();
			
			return page;
		} 
		catch (Exception ex) 
		{
			logger.trace(ex);
			
			model.addAttribute("info", "Login with your Username and Password");
			courseRegistrationReadWriteService.addErrorLog(ex.toString(), RegErrorMethod+"CourseRegistrationPageController", 
					"processLogout", registerNumber, IpAddress);
			courseRegistrationReadWriteService.updateRegistrationLogLogoutTimeStamp2(IpAddress,registerNumber);
			courseRegCommonFn.callCaptcha(request,response,session,model);
			session.setAttribute("CAPTCHA",session.getAttribute("CAPTCHA"));
			
			//page = "StudentLogin";
			page = "redirectpage";
			
			return page;
		}	
	}
	

	@PostMapping(value="ViewCredits")
	public String ViewCredits(Model model, String creditDetailshowFlag, HttpSession session, HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String semesterSubId = (String) session.getAttribute("SemesterSubId");
		String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
		
		String urlPage = "";
		int regCount = 0;
		float regCredit = 0;
		String IpAddress=(String) session.getAttribute("IpAddress");
		Integer maxCredit = (Integer) session.getAttribute("maxCredit");
		
		try
		{
			if(creditDetailshowFlag.equals("true"))
			{
				if (registerNumber!=null)
				{	
					regCount = wishlistRegistrationService.getRegisterNumberTCCount(semesterSubId, classGroupId, registerNumber);
					regCredit = wishlistRegistrationService.getRegisterNumberTotalCredits(semesterSubId, classGroupId, registerNumber);
					
					model.addAttribute("regCredit", regCredit);
					model.addAttribute("regCount", regCount);
					model.addAttribute("maxCredit", maxCredit);
					
					urlPage = "mainpages/MainPage :: creditsFragment";
				}
				else
				{
					model.addAttribute("flag", 1);
					urlPage = "redirectpage";
					return urlPage;
				}
			}
			else
			{
				model.addAttribute("creditDetailshowFlag",creditDetailshowFlag);
				urlPage = "mainpages/MainPage::creditsFragment";
				
			}
		}
		catch(Exception e)
		{
			logger.trace(e);
			
			courseRegistrationReadWriteService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationPageController", 
					"ViewCredits", registerNumber, IpAddress);
			courseRegistrationReadWriteService.updateRegistrationLogLogoutTimeStamp2(IpAddress,registerNumber);
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			return urlPage;			
		}		

		return urlPage;
	}
	
	@PostMapping("viewCurriculumCredits")
	public String viewCurriculumCredits(HttpSession session,Model model)
	{
		Float cgpa = 0F;
		String urlPage="";
		List<Object[]> cclCtgCreditList = new ArrayList<Object[]>();
		List<String> regnoList = new ArrayList<String>();
		
		Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
		int studyStartYear = (int) session.getAttribute("StudyStartYear");
		String studentStudySystem = (String) session.getAttribute("studentStudySystem");
		Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
		String semesterSubId = (String) session.getAttribute("SemesterSubId");
		String registerNo = (String) session.getAttribute("RegisterNumber");
		String OldRegNo = (String) session.getAttribute("OldRegNo");
		String IpAddress=(String) session.getAttribute("IpAddress");
		String studentCgpaData = (String) session.getAttribute("studentCgpaData");
		String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
		
		try 
		{
			if(registerNo!=null)
			{
				regnoList.add(registerNo);
				if ((OldRegNo != null) && (!OldRegNo.equals("")))
				{
					regnoList.add(OldRegNo);
				}
				cclCtgCreditList = programmeSpecializationCurriculumCreditService.getCurrentSemRegCurCtgCreditByRegisterNo(
			                   			programSpecId, studyStartYear, curriculumVersion, semesterSubId, regnoList, 
			                   			Arrays.asList(classGroupId));
				
				//Student CGPA Detail
				if ((studentCgpaData != null) && (!studentCgpaData.equals("")))
		    	{
					String[] studentCgpaArr = studentCgpaData.split("\\|");
								    	
					cgpa = Float.parseFloat(studentCgpaArr[2]);
					System.out.println("cgpaxxx"+cgpa);
		    	}
								
				model.addAttribute("cclCtgCreditList", cclCtgCreditList);
				model.addAttribute("studentStudySystem", studentStudySystem);
				model.addAttribute("tlCgpa", cgpa);
				
				urlPage = "mainpages/ViewCurriculumCredits::section";
			}
			else
			{
				model.addAttribute("flag", 1);
				urlPage = "redirectpage";
				return urlPage;
			}
		} 
		catch (Exception e) 
		{
			logger.trace(e);
			
			courseRegistrationReadWriteService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationPageController", 
					"viewCurriculumCredits", registerNo, IpAddress);
			courseRegistrationReadWriteService.updateRegistrationLogLogoutTimeStamp2(IpAddress,registerNo);
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			return urlPage;			
		}
		
		return urlPage;
	}
	
	@PostMapping("viewRegistered")
	public String viewRegistered(Model model, HttpSession session, HttpServletRequest request) 
	{
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress = (String) session.getAttribute("IpAddress");
		Integer maxCredit = (Integer) session.getAttribute("maxCredit");
		
		int allowStatus = 2, regCount = 0;
		Integer updateStatus = 1;
		float regCredit = 0;
		String urlPage = "", msg = null, infoMsg = "", checkCourseId = "";
		List<Object[]> courseRegistrationModel = new ArrayList<Object[]>();
								
		try
		{
			if (registerNumber!=null)
			{
				String semesterSubId = (String) session.getAttribute("SemesterSubId");
				Integer programSpecId = (Integer) session.getAttribute("ProgramSpecId");
				int studyStartYear = (int) session.getAttribute("StudyStartYear");
				Float curriculumVersion = (Float) session.getAttribute("curriculumVersion");
				String[] classGroupId = session.getAttribute("classGroupId").toString().split("/");
				
				Date startDate = (Date) session.getAttribute("startDate");
				Date endDate = (Date) session.getAttribute("endDate");
				String startTime = (String) session.getAttribute("startTime");
				String endTime = (String) session.getAttribute("endTime");
				
				String returnVal = courseRegistrationReadWriteService.AddorDropDateTimeCheck(startDate, endDate, startTime, endTime, 
										registerNumber, updateStatus, IpAddress);
				String[] statusMsg = returnVal.split("/");
				allowStatus = Integer.parseInt(statusMsg[0]);
				infoMsg = statusMsg[1];
				
				StudentsLoginDetailsModel studentsLoginDetailsModel = new StudentsLoginDetailsModel();
				studentsLoginDetailsModel = semesterMasterService.getStudentLoginDetailByRegisterNumber(registerNumber);
								
				switch(allowStatus)
				{
					case 1:

						if ((semesterSubId !=null) &&(registerNumber !=null))
						{
							courseRegistrationModel = wishlistRegistrationService.getByRegisterNumberAsObject(semesterSubId, classGroupId, registerNumber);
							if (!courseRegistrationModel.isEmpty())
							{
								for (Object[] obj : courseRegistrationModel) 
								{									
									regCredit = regCredit + Float.parseFloat(obj[10].toString());
									
									if (!obj[0].toString().equals(checkCourseId))
									{
										regCount++;
										checkCourseId = obj[0].toString();
									}
								}
							}
						}
						
						model.addAttribute("cDate", new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(new Date()));
						model.addAttribute("courseRegistrationModel", courseRegistrationModel);
						model.addAttribute("sdm", semesterMasterService.getSemesterDetailBySemesterSubId(semesterSubId));
						model.addAttribute("studentsLoginDetailsModel", studentsLoginDetailsModel);
						model.addAttribute("showFlag", 0);
						model.addAttribute("curriculumMapList", programmeSpecializationCurriculumDetailService.
								getCurriculumBySpecIdYearAndCCVersionAsMap(programSpecId, studyStartYear, curriculumVersion));
						
						model.addAttribute("maxCredit", maxCredit);
						model.addAttribute("regCredit", regCredit);
						model.addAttribute("regCount", regCount);
												
						session.removeAttribute("registrationOption");
						urlPage = "mainpages/ViewRegistered::section";
						
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
			
			courseRegistrationReadWriteService.addErrorLog(e.toString(), RegErrorMethod+"CourseRegistrationPageController", 
					"viewRegistered", registerNumber, IpAddress);
			courseRegistrationReadWriteService.updateRegistrationLogLogoutTimeStamp2(IpAddress,registerNumber);
			model.addAttribute("flag", 1);
			urlPage = "redirectpage";
			return urlPage;
		}
		
		return urlPage;
	}
	
	@PostMapping(value = "getSchoolWiseGuideList")
	public String getSchoolWiseGuideList(String guideSchoolOpt , Model model, HttpSession session, HttpServletRequest request) 
	{	
		
		String urlPage = "";
		String registerNumber = (String) session.getAttribute("RegisterNumber");
		String IpAddress=(String) session.getAttribute("IpAddress");
		
		try
		{
			Integer costCentreId = 0;
			if ((guideSchoolOpt != null) && (!guideSchoolOpt.equals("")))
			{
				costCentreId =Integer.parseInt(guideSchoolOpt);
			}
			List<EmployeeProfile> employeeList = semesterMasterService.getEmployeeProfileByCentreId(costCentreId);
			model.addAttribute("employeeList", employeeList);
			model.addAttribute("costCentreId", costCentreId);
			urlPage = "mainpages/ProjectRegistration::ProjectGuideFragment";			
		}
		catch(Exception e)
		{
			logger.trace(e);
			
			model.addAttribute("flag", 1);
			courseRegistrationReadWriteService.addErrorLog(e.toString()+" <-Guide School-> "+guideSchoolOpt, RegErrorMethod+"CourseRegistrationPageController", 
					"getSchoolWiseGuideList", registerNumber, IpAddress);
			courseRegistrationReadWriteService.updateRegistrationLogLogoutTimeStamp2(IpAddress,registerNumber);
			urlPage = "redirectpage";
			return urlPage;
		}
		
		return urlPage;	
	}
	
	List<String> getStartingTimeTableSlots(Integer patternId, List<PatternTimeMasterModel> list1)
	{		
		BigDecimal bg;
		List<Object[]> listMax = semesterMasterService.getTTPatternDetailMaxSlots(patternId);
		List<String> listTimeTableSlots = new ArrayList<String>();
		
		int fnMax = 0, anMax = 0, enMax=0;
		String sesMax = "";
		try
		{
			for (int m= 0; m< listMax.size(); m++)
			{
				sesMax =listMax.get(m)[1].toString(); 
				if (sesMax.equals("FN"))
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					fnMax = bg.intValue();
				}
				if (sesMax.equals("AN"))
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					anMax =bg.intValue();
				}
				if (sesMax.equals("EN"))
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					enMax = bg.intValue();
				}			
			}
		}
		catch(Exception ex)
		{
			logger.trace(ex);
		}
							
		//THEORY STARTING TIMINGS 
		int i = 1;
		for(PatternTimeMasterModel ls: list1)
		{
			String slName = ls.getPtmPkId().getSlotName().substring(0, 2);
			if (slName.equals("FN"))
			{
				listTimeTableSlots.add(ls.getStartingTime().toString().substring(0, 5));
				i++;
			}				
		}
		i = i-1;
		if (i < fnMax)
		{
			for (int j=1; j <= fnMax - i; j++)
			{
				listTimeTableSlots.add("-");
			}
		}
		
		listTimeTableSlots.add("Lunch");
		i=1;
		for(PatternTimeMasterModel ls: list1)
		{
			String slName = ls.getPtmPkId().getSlotName().substring(0, 2);
			if (slName.equals("AN"))
			{
				listTimeTableSlots.add(ls.getStartingTime().toString().substring(0, 5));
				i++;
			}
		}
		i = i-1;
		if (i < anMax)
		{
			for (int j=1; j <= anMax - i; j++)
			{
				listTimeTableSlots.add("-");
			}
		}
		
		
		i=1;
		for(PatternTimeMasterModel ls: list1)
		{
			String slName = ls.getPtmPkId().getSlotName().substring(0, 2);
			if (slName.equals("EN"))
			{
				listTimeTableSlots.add(ls.getStartingTime().toString().substring(0, 5));
				i++;
			}				
		}	
		i = i-1;
		if (i < enMax)
		{
			for (int j=1; j <= enMax - i; j++)
			{
				listTimeTableSlots.add("-");
			}
		}
		
							
		for (int k = 0; k < listTimeTableSlots.size(); k++)
		{
		
		}
		
		return listTimeTableSlots;
	}
	
	
	List<String> getEndingTimeTableSlots(Integer patternId, List<PatternTimeMasterModel> list1)
	{		
		BigDecimal bg;
		List<Object[]> listMax = semesterMasterService.getTTPatternDetailMaxSlots(patternId);
		List<String> listTimeTableSlots = new ArrayList<String>();
		
		int fnMax = 0, anMax = 0, enMax=0;
		String sesMax;
		try
		{
			for (int m= 0; m< listMax.size(); m++)
			{
				sesMax =listMax.get(m)[1].toString(); 
				if (sesMax.equals("FN"))
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					fnMax = bg.intValue();
				}
				if (sesMax.equals("AN"))
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					anMax =bg.intValue();
				}
				if (sesMax.equals("EN"))
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					enMax = bg.intValue();
				}			
			}
		}
		catch(Exception ex)
		{
			logger.trace(ex);
		}
		
		
		//THEORY STARTING TIMINGS 
		int i = 1;
		for(PatternTimeMasterModel ls: list1)
		{
			String slName = ls.getPtmPkId().getSlotName().substring(0, 2);
			if (slName.equals("FN"))
			{
				listTimeTableSlots.add(ls.getEndingTime().toString().substring(0, 5));
				i++;
			}				
		}
		i = i-1;
		if (i < fnMax)
		{
			for (int j=1; j <= fnMax - i; j++)
			{
				listTimeTableSlots.add("-");
			}
		}
		
		listTimeTableSlots.add("Lunch");
		i=1;
		for(PatternTimeMasterModel ls: list1)
		{
			String slName = ls.getPtmPkId().getSlotName().substring(0, 2);
			if (slName.equals("AN"))
			{
				listTimeTableSlots.add(ls.getEndingTime().toString().substring(0, 5));
				i++;
			}
		}
		i = i-1;
		if (i < anMax)
		{
			for (int j=1; j <= anMax - i; j++)
			{
				listTimeTableSlots.add("-");
			}
		}
		
		
		i=1;
		for(PatternTimeMasterModel ls: list1)
		{
			String slName = ls.getPtmPkId().getSlotName().substring(0, 2);
			if (slName.equals("EN"))
			{
				listTimeTableSlots.add(ls.getEndingTime().toString().substring(0, 5));
				i++;
			}				
		}	
		i = i-1;
		if (i < enMax)
		{
			for (int j=1; j <= enMax - i; j++)
			{
				listTimeTableSlots.add("-");
			}
		}
							
		return listTimeTableSlots;
	}
	
	List<Object[]> getTimeTableSlots(String semesterSubId, String registerNumber, Integer patternId,
						List<SlotTimeMasterModel> slotTimeMasterList) 
	{
		BigDecimal bg;
		List<Object[]> listMax = semesterMasterService.getTTPatternDetailMaxSlots(patternId);
		List<String> listTimeTableSlots = new ArrayList<String>();
		List<Object[]> listTimeTableSlots1 = new ArrayList<Object[]>();
	
		int fnMax = 0, anMax = 0, enMax = 0;
		String sesMax;
		try 
		{
			for (int m = 0; m < listMax.size(); m++) 
			{
				sesMax = listMax.get(m)[1].toString();
				if (sesMax.equals("FN")) 
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					fnMax = bg.intValue();
				}
				if (sesMax.equals("AN")) 
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					anMax = bg.intValue();
				}
				if (sesMax.equals("EN")) 
				{
					bg = new BigDecimal(listMax.get(m)[0].toString());
					enMax = bg.intValue();
				}
			}
		} 
		catch (Exception ex) 
		{
			logger.trace(ex);
		}
	
		int i = 1;
		
		List<Object[]> regSlots = courseRegistrationService.getCourseRegWlSlotByStudent(semesterSubId,
						registerNumber, patternId);
		Map<String, List<Object[]>> tempMap = new HashMap<>();
	
		for (Object[] parameters : regSlots) 
		{
			if (tempMap.containsKey(parameters[1])) 
			{
				List<Object[]> temp = tempMap.get(parameters[1]);
				temp.add(parameters);
				tempMap.put(parameters[1].toString(), temp);
			} 
			else 
			{
				List<Object[]> temp = new ArrayList<>();
				temp.add(parameters);
				tempMap.put(parameters[1].toString(), temp);
			}
		}
	
		for (SlotTimeMasterModel ls : slotTimeMasterList) 
		{
			String slName = ls.getSession();
			if (slName.equals("FN")) 
			{
				String[] tempArr = new String[2];
				tempArr[0] = ls.getStmPkId().getSlot();
				if (tempMap.containsKey(ls.getStmPkId().getWeekdays())) 
				{
					for (Object[] obj : tempMap.get(ls.getStmPkId().getWeekdays())) 
					{
						if (obj[0].equals(ls.getStmPkId().getSlot())) 
						{
							tempArr[0] = obj[2] + "-" + obj[3] + "-" + obj[0] + "-" + obj[4];
							tempArr[1] = "#CCFF33";
							break;
						} 
						else 
						{
							tempArr[1] = "";
						}
					}
				} 
				else 
				{
					tempArr[1] = "";
				}
	
				listTimeTableSlots1.add(tempArr);
				i++;
			}
		}
		
		i = i - 1;
		if (i < fnMax) 
		{
			for (int j = 1; j <= fnMax - i; j++) 
			{
				listTimeTableSlots.add("-");
				String[] tempArr = new String[2];
				tempArr[0] = "-";
				tempArr[1] = "";
				listTimeTableSlots1.add(tempArr);
			}
		}
	
		String[] tempArrLunch = new String[2];
		tempArrLunch[0] = "Lunch";
		tempArrLunch[1] = "#e2e2e2";
		listTimeTableSlots1.add(tempArrLunch);
		i = 1;
		for (SlotTimeMasterModel ls : slotTimeMasterList) 
		{
			String slName = ls.getSession();
			if (slName.equals("AN")) {
				String[] tempArr = new String[2];
				tempArr[0] = ls.getStmPkId().getSlot();
				if (tempMap.containsKey(ls.getStmPkId().getWeekdays())) 
				{
					for (Object[] obj : tempMap.get(ls.getStmPkId().getWeekdays())) 
					{
						if (obj[0].equals(ls.getStmPkId().getSlot())) 
						{
							tempArr[0] = obj[2] + "-" + obj[3] + "-" + obj[0] + "-" + obj[4];
							tempArr[1] = "#CCFF33";
							break;
						} 
						else 
						{
							tempArr[1] = "";
						}
					}
	
				} 
				else 
				{
					tempArr[1] = "";
				}
	
				listTimeTableSlots1.add(tempArr);
				i++;
			}
		}
		i = i - 1;
		if (i < anMax) 
		{
			for (int j = 1; j <= anMax - i; j++) 
			{
				listTimeTableSlots.add("-");
				String[] tempArr = new String[2];
				tempArr[0] = "-";
				tempArr[1] = "";
				listTimeTableSlots1.add(tempArr);
			}
		}
	
		i = 1;
		for (SlotTimeMasterModel ls : slotTimeMasterList) 
		{
			String slName = ls.getSession();
			if (slName.equals("EN")) 
			{
				String[] tempArr = new String[2];
				tempArr[0] = ls.getStmPkId().getSlot();
				if (tempMap.containsKey(ls.getStmPkId().getWeekdays())) 
				{
					for (Object[] obj : tempMap.get(ls.getStmPkId().getWeekdays())) 
					{
						if (obj[0].equals(ls.getStmPkId().getSlot())) 
						{
							tempArr[0] = obj[2] + "-" + obj[3] + "-" + obj[0] + "-" + obj[4];
							tempArr[1] = "#CCFF33";
							break;
						} 
						else 
						{
							tempArr[1] = "";
						}
					}
				} 
				else 
				{
					tempArr[1] = "";
				}
	
				listTimeTableSlots1.add(tempArr);
				i++;
			}
		}
		i = i - 1;
		if (i < enMax) 
		{
			for (int j = 1; j <= enMax - i; j++) 
			{
				listTimeTableSlots.add("-");
				String[] tempArr = new String[2];
				tempArr[0] = "-";
				tempArr[1] = "";
				listTimeTableSlots1.add(tempArr);
			}
		}
		
		for (int k = 0; k < listTimeTableSlots1.size(); k++)
		{
			
		}
		return listTimeTableSlots1;
	}
}
