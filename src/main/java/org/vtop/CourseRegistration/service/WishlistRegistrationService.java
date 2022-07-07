package org.vtop.CourseRegistration.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vtop.CourseRegistration.model.WishlistRegistrationModel;
import org.vtop.CourseRegistration.repository.WishlistRegistrationRepository;


@Service
@Transactional(readOnly=true)
public class WishlistRegistrationService
{	
	@Autowired private WishlistRegistrationRepository wishlistRegistrationRepository;
	
	
	public List<WishlistRegistrationModel> getByRegisterNumber(String semesterSubId, String[] classGroupId, String registerNumber)
	{
		return wishlistRegistrationRepository.findByRegisterNumber(semesterSubId, classGroupId, registerNumber);
	}
	
	public List<WishlistRegistrationModel> getByRegisterNumberCourseId(String semesterSubId, String[] classGroupId, 
												String registerNumber, String courseId)
	{
		return wishlistRegistrationRepository.findByRegisterNumberCourseId(semesterSubId, classGroupId, registerNumber, 
					courseId);
	}
	
	public List<WishlistRegistrationModel> getByRegisterNumberCourseCode(String semesterSubId, String[] classGroupId, 
												String registerNumber, String courseCode)
	{
		return wishlistRegistrationRepository.findByRegisterNumberCourseCode(semesterSubId, classGroupId, registerNumber, 
					courseCode);
	}
		
	public Integer getRegisterNumberTCCount(String semesterSubId, String[] classGroupId, String registerNumber)
	{
		Integer tempCount = 0;
			
		tempCount = wishlistRegistrationRepository.findRegisterNumberTCCount(semesterSubId, classGroupId, registerNumber);
		if (tempCount == null)
		{
			tempCount = 0;
		}
		
		return tempCount;
	}
	
	public Integer getRegisterNumberTotalCredits(String semesterSubId, String[] classGroupId, String registerNumber)
	{
		Integer tempCount = 0;
			
		tempCount = wishlistRegistrationRepository.findRegisterNumberTotalCredits(semesterSubId, classGroupId, registerNumber);
		if (tempCount == null)
		{
			tempCount = 0;
		}
		
		return tempCount;
	}
	
	
	public List<Object[]> getByClassGroupRegisterNumberAndCourseOption(String semesterSubId, List<String> classGroupId, 
								List<String> registerNumber, List<String> courseOptionCode)
	{
		return wishlistRegistrationRepository.findByClassGroupRegisterNumberAndCourseOption(semesterSubId, classGroupId, 
					registerNumber, courseOptionCode);
	}
	
	public List<Object[]> getByRegisterNumberAsObject(String semesterSubId, String[] classGroupId, String registerNumber)
	{
		return wishlistRegistrationRepository.findByRegisterNumberAsObject(semesterSubId, classGroupId, registerNumber);
	}
	
	public List<String> getCourseByRegisterNumberAndClassGroup(String semesterSubId, String[] classGroupId, 
								String registerNumber)
	{
		List<String> tempCourseIdList = new ArrayList<String>();
		
		tempCourseIdList = wishlistRegistrationRepository.findCourseByRegisterNumberAndClassGroup(semesterSubId, 
								classGroupId, registerNumber);
		if (tempCourseIdList.isEmpty())
		{
			tempCourseIdList.add("NONE");
		}
				
		return tempCourseIdList;
	}
		
	public List<Object[]> getWLRegistrationByClassGroupAndCourseCode(String semesterSubId, String[] classGroupId, 
								String registerNumber, String courseCode)
	{
		return wishlistRegistrationRepository.findWLRegistrationByClassGroupAndCourseCode(semesterSubId, classGroupId, 
					registerNumber, courseCode);
	}
	
	public List<Object[]> getWLCERegistrationByClassGroupAndCourseCode(String semesterSubId, String[] classGroupId, 
								String registerNumber, String courseCode)
	{
		return wishlistRegistrationRepository.findWLCERegistrationByClassGroupAndCourseCode(semesterSubId, classGroupId, 
					registerNumber, courseCode);
	}
	
	
	public Integer getCourseCountByRegisterNumberAndCourseOption(String semesterSubId, String[] classGroupId, 
						String registerNumber, List<String> courseOption)
	{
		Integer tempCount = 0;
		
		tempCount = wishlistRegistrationRepository.findCourseCountByRegisterNumberAndCourseOption(semesterSubId, 
						classGroupId, registerNumber, courseOption);
		if (tempCount == null)
		{
			tempCount = 0;
		}
		
		return tempCount;
	}
	
	public List<String> getRegisteredCourseByRegisterNumber(String semesterSubId, String[] classGroupId, List<String> registerNumber)
	{
		return wishlistRegistrationRepository.findRegisteredCourseByRegisterNumber(semesterSubId, classGroupId, registerNumber);
	}
	
	public List<String> getEquivalanceRegisteredCourseByRegisterNumber(String semesterSubId, String[] classGroupId, List<String> registerNumber)
	{
		return wishlistRegistrationRepository.findEquivalanceRegisteredCourseByRegisterNumber(semesterSubId, classGroupId, registerNumber);
	}
	
	public List<Object[]> getCourseOptionByRegisterNumberAndCourseCode(String semesterSubId, String[] classGroupId, 
								String registerNumber, String courseCode)
	{
		return wishlistRegistrationRepository.findCourseOptionByRegisterNumberAndCourseCode(semesterSubId, classGroupId, 
					registerNumber, courseCode);
	}

	public List<String> getUEWishlistCourseByRegisterNumberAndCourseOption(String semesterSubId, String[] classGroupId, 
							String registerNumber, String courseCategory)
	{
		List<String> tempCourseList = new ArrayList<String>();
	
		if (courseCategory.equals("UC"))
		{
			tempCourseList = wishlistRegistrationRepository.findUEWishlistCourseByRegisterNumberAndCourseOption(
								semesterSubId, classGroupId, registerNumber, Arrays.asList("RUCUE"));
		}
		else if (courseCategory.equals("PE"))
		{
			tempCourseList = wishlistRegistrationRepository.findUEWishlistCourseByRegisterNumberAndCourseOption(
								semesterSubId, classGroupId, registerNumber, Arrays.asList("RPEUE","RGA","HON"));
		}
		else if (courseCategory.equals("UE"))
		{
			tempCourseList = wishlistRegistrationRepository.findUEWishlistCourseByRegisterNumberAndCourseOption(
								semesterSubId, classGroupId, registerNumber, Arrays.asList("RUCUE","RPEUE","RGA","MIN"));
		}
	
		return tempCourseList;
	}
	
	public List<String> getBlockedCourseIdByRegisterNumberForDelete(String semesterSubId, String[] classGroupId, 
							String registerNumber)
	{
		List<String> tempCourseIdList = new ArrayList<String>();
		
		tempCourseIdList = wishlistRegistrationRepository.findBlockedCourseIdByRegisterNumberForDelete(semesterSubId, 
								classGroupId, registerNumber);
		if (tempCourseIdList.isEmpty())
		{
			tempCourseIdList.add("NONE");
		}
				
		return tempCourseIdList;
	}
	
	
	public List<Object[]> getWLRegistrationWithCEByClassGroupAndCourseCode(String semesterSubId, String[] classGroupId, 
								String registerNumber, String courseCode)
	{
		return wishlistRegistrationRepository.findWLRegistrationWithCEByClassGroupAndCourseCode(semesterSubId, classGroupId, 
					registerNumber, courseCode);
	}
	
	public List<String> getCourseFromWLRegistrationAndStudentHistoryBySemesterAndRegisterNoforCS(String semesterSubId, List<String> registerNumber, 
							String[] classGroupId)
	{
		return wishlistRegistrationRepository.findCourseFromWLRegistrationAndStudentHistoryBySemesterAndRegisterNoforCS(semesterSubId, 
						registerNumber, classGroupId);
	}
}

