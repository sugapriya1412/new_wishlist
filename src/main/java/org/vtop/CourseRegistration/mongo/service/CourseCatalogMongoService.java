package org.vtop.CourseRegistration.mongo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vtop.CoureRegistration.mongo.model.CourseCatalog;
import org.vtop.CoureRegistration.mongo.model.CourseEquivalanceCatalog;
import org.vtop.CoureRegistration.mongo.model.ProgramSpecializationCurriculumDetail;
import org.vtop.CoureRegistration.mongo.model.ResearchStudentCourseDetail;
import org.vtop.CoureRegistration.mongo.model.StudentHistory;
import org.vtop.CourseRegistration.mongo.repository.CourseCatalogMongoRepository;
import org.vtop.CourseRegistration.mongo.repository.CourseEquivalanceCatalogMongoRepository;
import org.vtop.CourseRegistration.mongo.repository.ProgramSpecializationCurriculumDetailMongoRepository;
import org.vtop.CourseRegistration.mongo.repository.ResearchStudentCourseDetailMongoRepository;
import org.vtop.CourseRegistration.mongo.repository.StudentHistoryMongoRepository;


@Service
public class CourseCatalogMongoService
{
	@Autowired private CourseCatalogMongoRepository courseCatalogMongoRepository;
	@Autowired private ProgramSpecializationCurriculumDetailMongoRepository programSpecializationCurriculumDetailMongoRepository;
	@Autowired private ResearchStudentCourseDetailMongoRepository researchStudentCourseDetailMongoRepository;
	@Autowired private StudentHistoryMongoRepository studentHistoryMongoRepository;
	@Autowired private CourseEquivalanceCatalogMongoRepository courseEquivalanceCatalogMongoRepository;
	
	private static final Logger logger = LogManager.getLogger(CourseCatalogMongoService.class);
	
		
	public CourseCatalog getByCourseId(String courseId)
	{
		return courseCatalogMongoRepository.findByCourseId(courseId); 
	}
	
		
	public List<CourseCatalog> getCourseListForRegistration(String registrationOption, String campusCode, 
										String[] courseSystem, List<Integer> egbGroupId, Integer programGroupId, 
										String semesterSubId, Integer programSpecId, String[] classGroupId, 
										String[] classType, Integer admissionYear, Float curriculumVersion, 
										String registerNumber, int searchType, String searchValue, 
										Integer studentGraduateYear, String programGroupCode, 
										String programSpecCode, String registrationMethod, String[] registerNumber2, 
										int PEUEAllowStatus, int evalPage, int evalPageSize, String costCentreCode)
	{
		List<CourseCatalog> returnModelList = new ArrayList<>();
		
		try
		{		
			int dataListFlag = 2;
			String programGroup = "", programGroupStart = "", programGroupMid = "", programGroupEnd = "";
			List<String> courseCodeList = new ArrayList<>();
			List<String> notCourseCodeList = new ArrayList<>();
			List<ProgramSpecializationCurriculumDetail> prgSplCurriculumDetailList = new ArrayList<>();
			List<ResearchStudentCourseDetail> researchStudentCourseDetailList = new ArrayList<>();
			List<StudentHistory> studentHistoryList = new ArrayList<>();
			List<CourseEquivalanceCatalog> courseEquivalanceList = new ArrayList<>();
			
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
			
			logger.trace("\n registrationOption: "+ registrationOption +" | srhType: "+ searchType 
					+" | dataListFlag: "+ dataListFlag +" | PEUEAllowStatus: "+ PEUEAllowStatus);	
			
			logger.trace("\n campusCode: "+ campusCode +" | programGroup: "+ programGroup 
				+" | semesterSubId: "+ semesterSubId +" | registerNumber: "+ registerNumber
				+" | searchValue: "+ searchValue +" | evalPage: "+ evalPage 
				+" | evalPageSize: "+ evalPageSize +" | programSpecId: "+ programSpecId 
				+" | admissionYear: "+ admissionYear +" | curriculumVersion: "+ curriculumVersion 
				+" | registrationMethod: "+ registrationMethod +" | costCentreCode: "+ costCentreCode 
				+" | programGroupCode: "+ programGroupCode +" | programSpecCode: "+ programSpecCode);
			
			logger.trace("\n courseSystem: "+ Arrays.toString(courseSystem));
			logger.trace("\n egbGroupId: "+ egbGroupId.toString());
			logger.trace("\n classGroupId: "+ Arrays.toString(classGroupId));
			logger.trace("\n classType: "+ Arrays.toString(classType));
			logger.trace("\n registerNumber2: "+ Arrays.toString(registerNumber2));
					
			
			if (dataListFlag == 1)
			{
				programGroupStart = ".*^"+ programGroup +"/.*";
				programGroupMid = ".*/"+ programGroup +"/.*";
				programGroupEnd = ".*/"+ programGroup +"$.*";
						
				switch(registrationOption)
				{						
					case "UE":
						prgSplCurriculumDetailList = programSpecializationCurriculumDetailMongoRepository.findBySpecIdAndAdmissionYear(
														programSpecId, admissionYear);
						if (!prgSplCurriculumDetailList.isEmpty())
						{
							courseCodeList = prgSplCurriculumDetailList.stream().map(e-> e.getCourseCode()).distinct().collect(Collectors.toList());
						}
						else
						{
							courseCodeList.add("NONE");
						}
						logger.trace("\n courseCodeList: "+ courseCodeList.toString());
						returnModelList = courseCatalogMongoRepository.findByCourseSystemGroupIdAndNINCourseCodeExceptCourseTypeAndEvalType(
												Arrays.asList(courseSystem), egbGroupId, programGroup, courseCodeList, programGroupStart, 
												programGroupMid, programGroupEnd, Arrays.asList("SS","ECA","PJT","OC"), Arrays.asList("IIP","LSM","TARP"));
						break;
						
					case "RGR":					
						studentHistoryList = studentHistoryMongoRepository.findByRegisterNumber(Arrays.asList(registerNumber2));
						if (!studentHistoryList.isEmpty())
						{
							notCourseCodeList = studentHistoryList.stream().map(e-> ((e.getCourseCode() == null) || (e.getCourseCode().isEmpty())) 
													? "NONE" : e.getCourseCode()).distinct().collect(Collectors.toList());
						}
						else
						{
							notCourseCodeList.add("NONE");
						}
						logger.trace("\n notCourseCodeList (Leve - 1): "+ notCourseCodeList.toString());
						
						courseEquivalanceList = courseEquivalanceCatalogMongoRepository.findByCourseCodeAndEquivalentCourseCode(notCourseCodeList);
						if (!courseEquivalanceList.isEmpty())
						{
							notCourseCodeList.addAll(courseEquivalanceList.stream().map(e-> ((e.getCourseCode() == null) 
									|| (e.getCourseCode().isEmpty())) ? "NONE" : e.getCourseCode()).distinct().collect(Collectors.toList()));
							notCourseCodeList.addAll(courseEquivalanceList.stream().map(e-> ((e.getEquivalentCourseCode() == null) 
									|| (e.getEquivalentCourseCode().isEmpty())) ? "NONE" : e.getEquivalentCourseCode())
									.distinct().collect(Collectors.toList()));
						}
						logger.trace("\n notCourseCodeList (Leve - 2): "+ notCourseCodeList.toString());
						
						if (!notCourseCodeList.isEmpty())
						{
							notCourseCodeList = notCourseCodeList.stream().distinct().collect(Collectors.toList());
						}
						logger.trace("\n notCourseCodeList (Leve - 3): "+ notCourseCodeList.toString());
						
						if ((programGroupCode.equals("RP")) && (admissionYear >= 2018))
						{
							researchStudentCourseDetailList = researchStudentCourseDetailMongoRepository.findByRegisterNumber(registerNumber);
							if (!researchStudentCourseDetailList.isEmpty())
							{
								courseCodeList = researchStudentCourseDetailList.stream().map(e-> e.getCourseCode()).distinct().collect(Collectors.toList());
							}
							else
							{
								courseCodeList.add("NONE");
							}
							returnModelList = courseCatalogMongoRepository.findByCourseSystemGroupIdAndINWNINCourseCodeExceptCourseType(Arrays.asList(courseSystem), 
													egbGroupId, programGroup, courseCodeList, notCourseCodeList, programGroupStart, programGroupMid, programGroupEnd, 
													Arrays.asList("SS","ECA","OC"));
						}
						else
						{
							returnModelList = courseCatalogMongoRepository.findByCourseSystemGroupIdAndNINCourseCodeExceptCourseType(Arrays.asList(courseSystem), 
													egbGroupId, programGroup, notCourseCodeList, programGroupStart, programGroupMid, programGroupEnd, 
													Arrays.asList("SS","ECA","OC"));
						}
						
						if (!returnModelList.isEmpty())
						{
							returnModelList = returnModelList.parallelStream().filter(e-> (!e.getCourseCode().substring(0, 4).equals("SET")))
													.collect(Collectors.toList());
						}
						break;
						
					case "RR":					
						studentHistoryList = studentHistoryMongoRepository.findFailedCourseByRegisterNumber(Arrays.asList(registerNumber2), 
													Arrays.asList("S","U","P","PASS","Pass","A","B","C","D","E","-"));
						if (!studentHistoryList.isEmpty())
						{
							courseCodeList = studentHistoryList.stream().map(e-> ((e.getCourseCode() == null) || (e.getCourseCode().isEmpty())) 
													? "NONE" : e.getCourseCode()).distinct().collect(Collectors.toList());
						}
						else
						{
							courseCodeList.add("NONE");
						}
						logger.trace("\n courseCodeList (Leve - 1): "+ courseCodeList.toString());
						
						courseEquivalanceList = courseEquivalanceCatalogMongoRepository.findByCourseCodeAndEquivalentCourseCode(courseCodeList);
						if (!courseEquivalanceList.isEmpty())
						{
							courseCodeList.addAll(courseEquivalanceList.stream().map(e-> ((e.getCourseCode() == null) 
									|| (e.getCourseCode().isEmpty())) ? "NONE" : e.getCourseCode()).distinct().collect(Collectors.toList()));
							courseCodeList.addAll(courseEquivalanceList.stream().map(e-> ((e.getEquivalentCourseCode() == null) 
									|| (e.getEquivalentCourseCode().isEmpty())) ? "NONE" : e.getEquivalentCourseCode())
									.distinct().collect(Collectors.toList()));
						}
						logger.trace("\n courseCodeList (Leve - 2): "+ courseCodeList.toString());
						
						if (!courseCodeList.isEmpty())
						{
							courseCodeList = courseCodeList.stream().distinct().collect(Collectors.toList());
						}
						logger.trace("\n courseCodeList (Leve - 3): "+ courseCodeList.toString());
						
						returnModelList = courseCatalogMongoRepository.findByCourseSystemGroupIdAndCourseCodeExceptCourseTypeForRR(Arrays.asList(courseSystem), 
												egbGroupId, programGroup, courseCodeList, programGroupStart, programGroupMid, programGroupEnd, 
												Arrays.asList("ECA","OC"));
						if (!returnModelList.isEmpty())
						{
							returnModelList = returnModelList.parallelStream().filter(e-> (!e.getCourseCode().substring(0, 4).equals("SET")))
													.collect(Collectors.toList());
						}
						break;
										
					case "FFCSCAL":						
						courseEquivalanceList = courseEquivalanceCatalogMongoRepository.findAll();
						if (!courseEquivalanceList.isEmpty())
						{
							courseCodeList.addAll(courseEquivalanceList.stream().map(e-> ((e.getCourseCode() == null) 
									|| (e.getCourseCode().isEmpty())) ? "NONE" : e.getCourseCode()).distinct().collect(Collectors.toList()));
							courseCodeList.addAll(courseEquivalanceList.stream().map(e-> ((e.getEquivalentCourseCode() == null) 
									|| (e.getEquivalentCourseCode().isEmpty())) ? "NONE" : e.getEquivalentCourseCode())
									.distinct().collect(Collectors.toList()));
						}
						logger.trace("\n courseCodeList (Leve - 1): "+ courseCodeList.toString());
						
						if (!courseCodeList.isEmpty())
						{
							courseCodeList = courseCodeList.stream().distinct().collect(Collectors.toList());
						}
						logger.trace("\n courseCodeList (Leve - 2): "+ courseCodeList.toString());
						
						returnModelList = courseCatalogMongoRepository.findByCourseSystemGroupIdAndCourseCodeExceptCourseTypeForFFCSToCAL(Arrays.asList("CAL"), 
												egbGroupId, programGroup, courseCodeList, programGroupStart, programGroupMid, programGroupEnd, 
												Arrays.asList("SS","ECA","PJT","OC"));					
						break;
					
					default:
						prgSplCurriculumDetailList = programSpecializationCurriculumDetailMongoRepository.findBySpecIdAdmissionYearAndCourseCategory(
													programSpecId, admissionYear, registrationOption);
						if (!prgSplCurriculumDetailList.isEmpty())
						{
							courseCodeList = prgSplCurriculumDetailList.stream().map(e-> e.getCourseCode()).distinct().collect(Collectors.toList());
						}
						else
						{
							courseCodeList.add("NONE");
						}
					
						returnModelList = courseCatalogMongoRepository.findByCourseSystemGroupIdAndCourseCodeExceptCourseTypeAndEvalType(
											Arrays.asList(courseSystem), egbGroupId, programGroup, courseCodeList, programGroupStart, 
											programGroupMid, programGroupEnd, Arrays.asList("SS","ECA","OC"));
						if (!returnModelList.isEmpty())
						{
							returnModelList = returnModelList.parallelStream().filter(e-> (!e.getCourseCode().substring(0, 4).equals("SET")))
													.collect(Collectors.toList());
						}
						break;
				}
			}
		}
		catch (Exception exception)
		{
			logger.trace("\n Exception: "+ exception);
		}
		
		return returnModelList;
	}	
}
