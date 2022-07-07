package org.vtop.CourseRegistration.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vtop.CourseRegistration.model.CourseCatalogModel;
import org.vtop.CourseRegistration.repository.CourseCatalogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service
@Transactional(readOnly=true)
public class CourseCatalogService
{
	@Autowired private CourseCatalogRepository courseCatalogRepository;
	@Autowired private StudentHistoryService studentHistoryService;
	
	private static final Logger logger = LogManager.getLogger(CourseCatalogService.class);
		
		
	public CourseCatalogModel getOne(String courseId)
	{
		return courseCatalogRepository.findById(courseId).orElse(null);
	}
	
	public CourseCatalogModel getLatestVersionByCourseCode(String courseCode)
	{
		return courseCatalogRepository.findLatestVersionByCourseCode(courseCode);
	}
	
	public CourseCatalogModel getOfferedCourseDetailByCourseCode(String semesterSubId, String[] classGroupId, 
									String[] classType, String courseCode)
	{
		return courseCatalogRepository.findOfferedCourseDetailByCourseCode(semesterSubId, classGroupId, 
					classType, courseCode);
	}
	
				
	//To get Course Owner's List
	public List<Object[]> getCourseCostCentre (String campus)
	{
		return courseCatalogRepository.findCourseCostCentre(campus);
	}
	
	//Compulsory Course Pagination
	public Page<CourseCatalogModel> getCompulsoryCoursePagination(String campusCode, String[] courseSystem, 
										List<Integer> egbGroupId, String groupCode, String semesterSubId, 
										String[] classGroupId, String[] classType, List<String> courseCode, 
										String progGroupCode, String progSpecCode, String costCentreCode, 
										Pageable pageable)
	{
		return courseCatalogRepository.findCompulsoryCourseAsPage(campusCode, courseSystem, egbGroupId, 
					groupCode, semesterSubId, courseCode, pageable);
	}
	
	
	public List<CourseCatalogModel> getCourseListForRegistration(String registrationOption, String campusCode, 
										String[] courseSystem, List<Integer> egbGroupId, Integer programGroupId, 
										String semesterSubId, Integer programSpecId, String[] classGroupId, 
										String[] classType, Integer admissionYear, Float curriculumVersion, 
										String registerNumber, int searchType, String searchValue, 
										Integer studentGraduateYear, String programGroupCode, 
										String programSpecCode, String registrationMethod, String[] registerNumber2, 
										int PEUEAllowStatus, int evalPage, int evalPageSize, String costCentreCode)
	{
		List<CourseCatalogModel> tempList = new ArrayList<CourseCatalogModel>();
				
		int dataListFlag = 2;
		String programGroup = "";
		List<String> courseCode = new ArrayList<String>();
		
		if ((registrationOption != null) && (!registrationOption.equals("")))
		{
			if (registrationOption.equals("PE") || registrationOption.equals("UE") 
					|| registrationOption.equals("BC") || registrationOption.equals("NC"))
			{
				if (PEUEAllowStatus == 1)
				{
					dataListFlag = 1;
				}
			}
			else
			{
				dataListFlag = 1;
			}
		}
		
		if ((searchType == 2) || (searchType == 3))
		{
			if ((searchValue == null) || (searchValue.equals("")))
			{
				searchValue = "NONE";
			}
			else
			{
				searchValue = searchValue.toUpperCase();
			}
		}
		
		if (programGroupId == null)
		{
			programGroup = "NONE";
		}
		else
		{
			programGroup = programGroupId.toString();
		}
		
		
		logger.trace("registrationOption: "+ registrationOption +" | srhType: "+ searchType 
				+" | dataListFlag: "+ dataListFlag +" | PEUEAllowStatus: "+ PEUEAllowStatus);	
		
		logger.trace("campusCode: "+ campusCode +" | programGroup: "+ programGroup 
			+" | semesterSubId: "+ semesterSubId +" | registerNumber: "+ registerNumber
			+" | searchValue: "+ searchValue +" | evalPage: "+ evalPage 
			+" | evalPageSize: "+ evalPageSize +" | programSpecId: "+ programSpecId 
			+" | admissionYear: "+ admissionYear +" | curriculumVersion: "+ curriculumVersion 
			+" | registrationMethod: "+ registrationMethod +" | costCentreCode: "+ costCentreCode 
			+" | programGroupCode: "+ programGroupCode +" | programSpecCode: "+ programSpecCode);
		
		logger.trace("courseSystem: "+ courseSystem);
		logger.trace("egbGroupId: "+ egbGroupId);
		logger.trace("classGroupId: "+ classGroupId);
		logger.trace("classType: "+ classType);
		logger.trace("registerNumber2: "+ registerNumber2);
				
		
		if (dataListFlag == 1)
		{
			switch(registrationOption)
			{						
				case "UE":
					switch(searchType)
					{
						case 1:
							tempList = courseCatalogRepository.findCurriculumUEByCourseCode(campusCode, courseSystem, 
											egbGroupId, programGroup, semesterSubId, programSpecId, admissionYear, 
											curriculumVersion, searchValue);
							break;
						default:
							tempList = courseCatalogRepository.findCurriculumUE(campusCode, courseSystem, egbGroupId, 
											programGroup, semesterSubId, programSpecId, admissionYear, curriculumVersion);
							break;
					}
					break;
					
				case "RGR":
					if ((programGroupCode.equals("RP")) && (admissionYear >= 2018))
					{
						courseCode = studentHistoryService.getRPCourseWorkByRegisterNumber(registerNumber);
						tempList = courseCatalogRepository.findRegularCourseByCourseCodeForResearch(campusCode, 
										courseSystem, egbGroupId, programGroup, semesterSubId, registerNumber2, courseCode);
					}
					else
					{
						switch(searchType)
						{
							case 1:
								tempList = courseCatalogRepository.findRegularCourseByCourseCode(campusCode, courseSystem, 
												egbGroupId, programGroup, semesterSubId, registerNumber2, searchValue);
								break;
							default:
								tempList = courseCatalogRepository.findRegularCourse(campusCode, courseSystem, egbGroupId, 
												programGroup, semesterSubId, registerNumber2);
								break;
						}
					}
					break;
					
				case "RR":
					switch(searchType)
					{
						case 1:
							tempList = courseCatalogRepository.findRRCourseByCourseCode(campusCode, courseSystem, 
											egbGroupId, programGroup, semesterSubId, registerNumber2, searchValue);
							break;
						default:
							tempList = courseCatalogRepository.findRRCourse(campusCode, courseSystem, egbGroupId, 
											programGroup, semesterSubId, registerNumber2);
							break;
					}
					break;
									
				case "FFCSCAL":
					switch(searchType)
					{
						case 1:
							tempList = courseCatalogRepository.findCALToFFCSCEByCourseCode(campusCode, egbGroupId, 
											programGroup, semesterSubId, searchValue);
							break;
						default:
							tempList = courseCatalogRepository.findCALToFFCSCE(campusCode, egbGroupId, programGroup,
												semesterSubId);
							break;
					}
					break;
				
				default:
					switch(searchType)
					{
						case 1:
							tempList = courseCatalogRepository.findCurriculumPCPEUCByCourseCode(campusCode, courseSystem, 
											egbGroupId, programGroup, semesterSubId, programSpecId, admissionYear, 
											registrationOption, curriculumVersion, searchValue);
							break;
						default:
							tempList = courseCatalogRepository.findCurriculumPCPEUC(campusCode, courseSystem, egbGroupId, 
											programGroup, semesterSubId, programSpecId, admissionYear, registrationOption, 
											curriculumVersion);
							break;
					}
					break;
			}
		}
		
		return tempList;
	}
	
	public String getTotalPageAndIndex(int dataSize, int pageSize, int pageNumber)
	{
		int totalPage = 0, fromIndex = 0, toIndex = 0;
		double calcTotalPage = 0;
		
		if (pageSize > 0)
		{
			calcTotalPage = (double)dataSize / (double)pageSize;
			totalPage = (int) Math.ceil(calcTotalPage);
		}
		
		if (pageNumber <= 0)
		{
			pageNumber = 0;
		}
		else if (pageNumber >= totalPage)
		{
			pageNumber = totalPage - 1;
		}
				
		if (totalPage > 0)
		{
			fromIndex = pageNumber * pageSize;
			toIndex = fromIndex + pageSize;
			if (toIndex > dataSize)
			{
				toIndex = dataSize;
			}
		}
						
		return totalPage +"|"+ fromIndex +"|"+ toIndex;
	}
}
