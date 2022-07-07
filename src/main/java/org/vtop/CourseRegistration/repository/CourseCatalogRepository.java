package org.vtop.CourseRegistration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.vtop.CourseRegistration.model.CourseCatalogModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Repository
public interface CourseCatalogRepository extends JpaRepository<CourseCatalogModel, String>
{	
	@Query("select a from CourseCatalogModel a where (a.code, a.courseVersion) in "+
			"(select b.code, max(b.courseVersion) from CourseCatalogModel b where b.code=?1 and b.status=0 "+
			"group by b.code)")
	CourseCatalogModel findLatestVersionByCourseCode(String courseCode);
	
	//To get Course Owner's List
	@Query("select distinct a.ownerCode, b.code, b.description from CourseCatalogModel a, CostCentre b "+
			"where a.campusCode=?1 and b.centreId=to_number(a.ownerCode,'9999') and a.campusCode=b.campusCode "+
			"order by b.code")
	List<Object[]> findCourseCostCentre(String campus);
		
	@Query("select a from CourseCatalogModel a where a.courseId=(select distinct b.courseId from CourseAllocationModel b "+
			"where b.semesterSubId=?1 and b.clsGrpMasterGroupId in (?2) and b.classType in (?3) and "+
			"b.courseCatalogModel.code=?4 and b.lockStatus=0) order by a.courseId")
	CourseCatalogModel findOfferedCourseDetailByCourseCode(String semesterSubId, String[] classGroupId, String[] classType, 
							String courseCode);
	
	
	//For Registration Purpose - Course Display
	//Regular Course with out search as List
	@Query("select a from CourseCatalogModel a where a.campusCode=?1 and a.courseSystem in (?2) and "+
			"(a.groupId in (?3) or (a.groupCode=?4 or a.groupCode like ?4||'/%' or a.groupCode "+
			"like '%/'||?4||'/%' or a.groupCode like '%/'||?4)) and a.genericCourseType not in ('SS','ECA','OC') "+
			"and (a.code not like 'SET%') and a.status=0 and a.courseId in "+
			"(select b.courseSchedulingPKId.courseId from CourseSchedulingModel b where "+
			"b.courseSchedulingPKId.semesterSubId=?5 and b.status=0) and "+
			"a.code not in (select (case when (c.courseCode is null) then 'NONE' else c.courseCode end) from "+
			"StudentHistoryModel c where c.studentHistoryPKId.registerNumber in (?6)) and a.code not in "+
			"(select d1.equivalentCourseCode from CourseEquivalancesModel d1 where d1.courseCode in "+
			"(select (case when (d2.courseCode is null) then 'NONE' else d2.courseCode end) from StudentHistoryModel d2 "+
			"where d2.studentHistoryPKId.registerNumber in (?6))) and a.code not in "+
			"(select e1.courseCode from CourseEquivalancesModel e1 where e1.equivalentCourseCode in "+
			"(select (case when (e2.courseCode is null) then 'NONE' else e2.courseCode end) from StudentHistoryModel e2 "+
			"where e2.studentHistoryPKId.registerNumber in (?6))) order by a.ownerCode, a.code, a.courseVersion")
	List<CourseCatalogModel> findRegularCourse(String campusCode, String[] courseSystem, List<Integer> egbGroupId, 
								String groupCode, String semesterSubId, String[] registerNumber);
	
		
	//Regular Course with Course Code Search as List
	@Query("select a from CourseCatalogModel a where a.campusCode=?1 and a.courseSystem in (?2) and (a.groupId in (?3) "+
			"or (a.groupCode=?4 or a.groupCode like ?4||'/%' or a.groupCode like '%/'||?4||'/%' or a.groupCode "+
			"like '%/'||?4)) and (a.code like ?7) and a.genericCourseType not in ('SS','ECA','OC') and (a.code not "+
			"like 'SET%') and a.status=0 and a.courseId in (select b.courseSchedulingPKId.courseId from CourseSchedulingModel b "+ 
			"where b.courseSchedulingPKId.semesterSubId=?5 and b.status=0) and "+
			"a.code not in (select (case when (c.courseCode is null) then 'NONE' else c.courseCode end) from "+
			"StudentHistoryModel c where c.studentHistoryPKId.registerNumber in (?6)) and a.code not in "+
			"(select d1.equivalentCourseCode from CourseEquivalancesModel d1 where d1.courseCode in "+
			"(select (case when (d2.courseCode is null) then 'NONE' else d2.courseCode end) from StudentHistoryModel d2 "+
			"where d2.studentHistoryPKId.registerNumber in (?6))) and a.code not in "+
			"(select e1.courseCode from CourseEquivalancesModel e1 where e1.equivalentCourseCode in "+
			"(select (case when (e2.courseCode is null) then 'NONE' else e2.courseCode end) from StudentHistoryModel e2 "+
			"where e2.studentHistoryPKId.registerNumber in (?6))) order by a.ownerCode, a.code, a.courseVersion")
	List<CourseCatalogModel> findRegularCourseByCourseCode(String campusCode, String[] courseSystem, List<Integer> egbGroupId, 
								String groupCode, String semesterSubId,	String[] registerNumber, String searchval);
	
	
	//Regular Course with Course Code - for Research Program as List
	@Query("select a from CourseCatalogModel a where a.campusCode=?1 and a.courseSystem in (?2) and (a.groupId in (?3) "+
			"or (a.groupCode=?4 or a.groupCode like ?4||'/%' or a.groupCode like '%/'||?4||'/%' or a.groupCode "+
			"like '%/'||?4)) and a.genericCourseType not in ('SS','ECA','OC') and (a.code not like 'SET%') and "+
			"a.status=0 and a.code in (?7) and a.courseId in (select b.courseSchedulingPKId.courseId from CourseSchedulingModel b "+ 
			"where b.courseSchedulingPKId.semesterSubId=?5 and b.status=0) and "+
			"a.code not in (select (case when (c.courseCode is null) then 'NONE' else c.courseCode end) from "+
			"StudentHistoryModel c where c.studentHistoryPKId.registerNumber in (?6)) and a.code not in "+
			"(select d1.equivalentCourseCode from CourseEquivalancesModel d1 where d1.courseCode in "+
			"(select (case when (d2.courseCode is null) then 'NONE' else d2.courseCode end) from StudentHistoryModel d2 "+
			"where d2.studentHistoryPKId.registerNumber in (?6))) and a.code not in "+
			"(select e1.courseCode from CourseEquivalancesModel e1 where e1.equivalentCourseCode in "+
			"(select (case when (e2.courseCode is null) then 'NONE' else e2.courseCode end) from StudentHistoryModel e2 "+
			"where e2.studentHistoryPKId.registerNumber in (?6))) order by a.ownerCode, a.code, a.courseVersion")
	List<CourseCatalogModel> findRegularCourseByCourseCodeForResearch(String campusCode, String[] courseSystem, 
								List<Integer> egbGroupId, String groupCode, String semesterSubId, String[] registerNumber, 
								List<String> courseCode);
	
			
	//Re-registered Course with out search as List
	@Query("select a from CourseCatalogModel a where a.campusCode=?1 and a.courseSystem in (?2) and (a.groupId in (?3) "+
			"or (a.groupCode=?4 or a.groupCode like ?4||'/%' or a.groupCode like '%/'||?4||'/%' or a.groupCode "+
			"like '%/'||?4)) and a.genericCourseType not in ('ECA','OC') and (a.code not like 'SET%') and a.status=0 "+
			"and a.courseId in (select b.courseSchedulingPKId.courseId from CourseSchedulingModel b where "+
			"b.courseSchedulingPKId.semesterSubId=?5 and b.status=0) and (a.code in "+
			"(select (case when (c.courseCode is null) then 'NONE' else c.courseCode end) from StudentHistoryModel c "+
			"where c.studentHistoryPKId.registerNumber in (?6) and c.courseTypeComponentModel.component in (1,3) "+
			"and c.grade not in ('S','U','P','PASS','Pass','A','B','C','D','E')) or a.code in "+
			"(select d1.equivalentCourseCode from CourseEquivalancesModel d1 where d1.courseCode in "+
			"(select (case when (d2.courseCode is null) then 'NONE' else d2.courseCode end) from StudentHistoryModel d2 "+
			"where d2.studentHistoryPKId.registerNumber in (?6) and d2.courseTypeComponentModel.component in (1,3) "+
			"and d2.grade not in ('S','U','P','PASS','Pass','A','B','C','D','E'))) or a.code in "+
			"(select e1.courseCode from CourseEquivalancesModel e1 where e1.equivalentCourseCode in "+
			"(select (case when (e2.courseCode is null) then 'NONE' else e2.courseCode end) from StudentHistoryModel e2 "+
			"where e2.studentHistoryPKId.registerNumber in (?6) and e2.courseTypeComponentModel.component in (1,3) "+
			"and e2.grade not in ('S','U','P','PASS','Pass','A','B','C','D','E')))) order by a.ownerCode, a.code, "+
			"a.courseVersion")
	List<CourseCatalogModel> findRRCourse(String campusCode, String[] courseSystem, List<Integer> egbGroupId, 
								String groupCode, String semesterSubId, String[] registerNumber);
	
	
	//Re-registered Course Pagination with Course Code Search as List
	@Query("select a from CourseCatalogModel a where a.campusCode=?1 and a.courseSystem in (?2) and (a.groupId in (?3) "+
			"or (a.groupCode=?4 or a.groupCode like ?4||'/%' or a.groupCode like '%/'||?4||'/%' or a.groupCode "+
			"like '%/'||?4)) and (a.code like ?7) and a.genericCourseType not in ('ECA','OC') and (a.code not "+
			"like 'SET%') and a.status=0 and a.courseId in (select b.courseSchedulingPKId.courseId from CourseSchedulingModel b "+ 
			"where b.courseSchedulingPKId.semesterSubId=?5 and b.status=0) and "+
			"(a.code in (select (case when (c.courseCode is null) then 'NONE' else c.courseCode end) from StudentHistoryModel c "+
			"where c.studentHistoryPKId.registerNumber in (?6) and c.courseTypeComponentModel.component in (1,3) and "+
			"c.grade not in ('S','U','P','PASS','Pass','A','B','C','D','E')) or a.code in "+
			"(select d1.equivalentCourseCode from CourseEquivalancesModel d1 where d1.courseCode in "+
			"(select (case when (d2.courseCode is null) then 'NONE' else d2.courseCode end) from StudentHistoryModel d2 "+
			"where d2.studentHistoryPKId.registerNumber in (?6) and d2.courseTypeComponentModel.component in (1,3) "+
			"and d2.grade not in ('S','U','P','PASS','Pass','A','B','C','D','E'))) or a.code in "+
			"(select e1.courseCode from CourseEquivalancesModel e1 where e1.equivalentCourseCode in "+
			"(select (case when (e2.courseCode is null) then 'NONE' else e2.courseCode end) from StudentHistoryModel e2 "+
			"where e2.studentHistoryPKId.registerNumber in (?6) and e2.courseTypeComponentModel.component in (1,3) "+
			"and e2.grade not in ('S','U','P','PASS','Pass','A','B','C','D','E')))) order by a.ownerCode, a.code, "+
			"a.courseVersion")
	List<CourseCatalogModel> findRRCourseByCourseCode(String campusCode, String[] courseSystem, List<Integer> egbGroupId, 
								String groupCode, String semesterSubId, String[] registerNumber, String searchval);
	

	//FFCS to CAL Course Equivalence with out Search as List
	@Query("select a from CourseCatalogModel a where a.campusCode=?1 and a.courseSystem='CAL' and (a.groupId in (?2) "+
			"or (a.groupCode=?3 or a.groupCode like ?3||'/%' or a.groupCode like '%/'||?3||'/%' or a.groupCode "+
			"like '%/'||?3)) and a.genericCourseType not in ('SS','ECA','PJT','OC') and a.status=0 and a.courseId in "+
			"(select b.courseSchedulingPKId.courseId from CourseSchedulingModel b where b.courseSchedulingPKId.semesterSubId=?4 "+
			"and b.status=0) and (a.code in (select c.courseCode from CourseEquivalancesModel c) or a.code in "+
			"(select d.equivalentCourseCode from CourseEquivalancesModel d)) order by a.ownerCode, a.code, a.courseVersion")
	List<CourseCatalogModel> findCALToFFCSCE(String campusCode, List<Integer> egbGroupId, String groupCode, String semesterSubId);
	
	
	//FFCS to CAL Course Equivalence with Course Code as List
	@Query("select a from CourseCatalogModel a where a.campusCode=?1 and a.courseSystem='CAL' and (a.groupId in (?2) "+
			"or (a.groupCode=?3 or a.groupCode like ?3||'/%' or a.groupCode like '%/'||?3||'/%' or a.groupCode "+
			"like '%/'||?3)) and (a.code like ?5) and a.genericCourseType not in ('SS','ECA','PJT','OC') and "+
			"a.status=0 and a.courseId in (select b.courseSchedulingPKId.courseId from CourseSchedulingModel b where "+
			"b.courseSchedulingPKId.semesterSubId=?4 and b.status=0) and "+
			"(a.code in (select c.courseCode from CourseEquivalancesModel c) or a.code in "+
			"(select d.equivalentCourseCode from CourseEquivalancesModel d)) order by a.ownerCode, a.code, a.courseVersion")
	List<CourseCatalogModel> findCALToFFCSCEByCourseCode(String campusCode, List<Integer> egbGroupId, String groupCode, 
								String semesterSubId, String searchval);
	

	//Compulsory Courses
	//As Page
	@Query("select a from CourseCatalogModel a where a.campusCode=?1 and a.courseSystem in (?2) and (a.groupId in (?3) "+
			"or (a.groupCode=?4 or a.groupCode like ?4||'/%' or a.groupCode like '%/'||?4||'/%' or a.groupCode "+
			"like '%/'||?4)) and a.code in (?6) and a.status=0 and a.courseId in "+
			"(select b.courseSchedulingPKId.courseId from CourseSchedulingModel b where b.courseSchedulingPKId.semesterSubId=?5 "+
			"and b.status=0) order by a.ownerCode, a.code, a.courseVersion")
	Page<CourseCatalogModel> findCompulsoryCourseAsPage(String campusCode, String[] courseSystem, List<Integer> egbGroupId, 
								String groupCode, String semesterSubId, List<String> courseCode, Pageable pageable);
	
		
	//Curriculum based course with out Search - Except UE category as List
	@Query("select a from CourseCatalogModel a where a.campusCode=?1 and a.courseSystem in (?2) and (a.groupId in (?3) "+
			"or (a.groupCode=?4 or a.groupCode like ?4||'/%' or a.groupCode like '%/'||?4||'/%' or a.groupCode "+
			"like '%/'||?4)) and a.genericCourseType not in ('SS','ECA','OC') and (a.code not like 'SET%') and a.status=0 "+
			"and a.courseId in (select b.courseSchedulingPKId.courseId from CourseSchedulingModel b where "+
			"b.courseSchedulingPKId.semesterSubId=?5 and b.status=0) and (a.code in "+
			"(select d.code from ProgrammeSpecializationCurriculumDetailModel c, CourseCatalogModel d where "+
			"c.psccdPkId.specializationId=?6 and c.psccdPkId.admissionYear=?7 and c.psccdPkId.curriculumVersion=?9 and "+
			"c.catalogType='CC' and c.courseCategory=?8 and c.status=0 and c.psccdPkId.courseBasketId=d.courseId) or "+
			"a.code in (select d.courseCatalogModel.code from ProgrammeSpecializationCurriculumDetailModel c, "+
			"BasketCourseCatalogModel d where c.psccdPkId.specializationId=?6 and c.psccdPkId.admissionYear=?7 "+
			"and c.psccdPkId.curriculumVersion=?9 and c.catalogType='BC' and c.courseCategory=?8 and c.status=0 and "+
			"d.status=0 and c.psccdPkId.courseBasketId=d.bccPkId.basketId)) order by a.ownerCode, a.code, a.courseVersion")
	List<CourseCatalogModel> findCurriculumPCPEUC(String campusCode, String[] courseSystem, List<Integer> egbGroupId, 
								String groupCode, String semesterSubId, Integer specId, Integer studYear, String courseCategory, 
								Float curriculumVersion);
	
	
	//Curriculum based course with Course Code Search - Except UE category as List
	@Query("select a from CourseCatalogModel a where a.campusCode=?1 and a.courseSystem in (?2) and (a.groupId in (?3) "+
			"or (a.groupCode=?4 or a.groupCode like ?4||'/%' or a.groupCode like '%/'||?4||'/%' or a.groupCode "+
			"like '%/'||?4)) and (a.code like ?10) and a.genericCourseType not in ('SS','ECA','OC') and "+
			"(a.code not like 'SET%') and a.status=0 and a.courseId in "+
			"(select b.courseSchedulingPKId.courseId from CourseSchedulingModel b where b.courseSchedulingPKId.semesterSubId=?5 "+
			"and b.status=0) and (a.code in (select d.code from ProgrammeSpecializationCurriculumDetailModel c, "+
			"CourseCatalogModel d where c.psccdPkId.specializationId=?6 and c.psccdPkId.admissionYear=?7 and "+
			"c.psccdPkId.curriculumVersion=?9 and c.catalogType='CC' and c.courseCategory=?8 and "+
			"c.status=0 and c.psccdPkId.courseBasketId=d.courseId) or a.code "+
			"in (select d.courseCatalogModel.code from ProgrammeSpecializationCurriculumDetailModel c, "+
			"BasketCourseCatalogModel d where c.psccdPkId.specializationId=?6 and c.psccdPkId.admissionYear=?7 "+
			"and c.psccdPkId.curriculumVersion=?9 and c.catalogType='BC' and c.courseCategory=?8 and "+
			"c.status=0 and d.status=0 and c.psccdPkId.courseBasketId=d.bccPkId.basketId)) "+
			"order by a.ownerCode, a.code, a.courseVersion")
	List<CourseCatalogModel> findCurriculumPCPEUCByCourseCode(String campusCode, String[] courseSystem, List<Integer> egbGroupId, 
								String groupCode, String semesterSubId, Integer specId, Integer studYear, String courseCategory, 
								Float curriculumVersion, String searchval);
	

	//Curriculum based course with out search - Only UE category as List
	@Query("select a from CourseCatalogModel a where a.campusCode=?1 and a.courseSystem in (?2) and (a.groupId in (?3) "+
			"or (a.groupCode=?4 or a.groupCode like ?4||'/%' or a.groupCode like '%/'||?4||'/%' or a.groupCode "+
			"like '%/'||?4)) and a.genericCourseType not in ('SS','ECA','PJT','OC') and (a.code not like 'SET%') and "+
			"a.evaluationType not in ('IIP','LSM','TARP') and a.status=0 and a.courseId in "+
			"(select b.courseSchedulingPKId.courseId from CourseSchedulingModel b where b.courseSchedulingPKId.semesterSubId=?5 "+ 
			"and b.status=0) and a.code not in (select d.code from ProgrammeSpecializationCurriculumDetailModel c, "+
			"CourseCatalogModel d where c.psccdPkId.specializationId=?6 and c.psccdPkId.admissionYear=?7 and "+
			"c.psccdPkId.curriculumVersion=?8 and c.status=0 and c.psccdPkId.courseBasketId=d.courseId) and "+
			"a.code not in (select d.courseCatalogModel.code from ProgrammeSpecializationCurriculumDetailModel c, "+
			"BasketCourseCatalogModel d where c.psccdPkId.specializationId=?6 and c.psccdPkId.admissionYear=?7 and "+
			"c.psccdPkId.curriculumVersion=?8 and c.catalogType='BC' and c.status=0 and d.status=0 and "+
			"c.psccdPkId.courseBasketId=d.bccPkId.basketId) order by a.ownerCode, a.code, a.courseVersion")
	List<CourseCatalogModel> findCurriculumUE(String campusCode, String[] courseSystem, List<Integer> egbGroupId, 
								String groupCode, String semesterSubId, Integer specId, Integer studYear, 
								Float curriculumVersion);
	
	
	//Curriculum based course with Course Code search - Only UE category as List
	@Query("select a from CourseCatalogModel a where a.campusCode=?1 and a.courseSystem in (?2) and (a.groupId in (?3) "+
			"or (a.groupCode=?4 or a.groupCode like ?4||'/%' or a.groupCode like '%/'||?4||'/%' or a.groupCode "+
			"like '%/'||?4)) and (a.code like ?9) and a.genericCourseType not in ('SS','ECA','PJT','OC') and "+
			"(a.code not like 'SET%') and a.evaluationType not in ('IIP','LSM','TARP') and a.status=0 and a.courseId in "+
			"(select b.courseSchedulingPKId.courseId from CourseSchedulingModel b where b.courseSchedulingPKId.semesterSubId=?5 "+ 
			"and b.status=0) and a.code not in (select d.code from ProgrammeSpecializationCurriculumDetailModel c, "+
			"CourseCatalogModel d where c.psccdPkId.specializationId=?6 and c.psccdPkId.admissionYear=?7 and "+
			"c.psccdPkId.curriculumVersion=?8 and c.status=0 and c.psccdPkId.courseBasketId=d.courseId) and a.code not in "+
			"(select d.courseCatalogModel.code from ProgrammeSpecializationCurriculumDetailModel c, "+
			"BasketCourseCatalogModel d where c.psccdPkId.specializationId=?6 and c.psccdPkId.admissionYear=?7 "+
			"and c.psccdPkId.curriculumVersion=?8 and c.catalogType='BC' and c.status=0 and d.status=0 and "+
			"c.psccdPkId.courseBasketId=d.bccPkId.basketId) order by a.ownerCode, a.code, a.courseVersion")
	List<CourseCatalogModel> findCurriculumUEByCourseCode(String campusCode, String[] courseSystem, List<Integer> egbGroupId, 
								String groupCode, String semesterSubId, Integer specId, Integer studYear, Float curriculumVersion, 
								String searchval);
	
	
	//Soft Skill Courses - CAL as List
	@Query("select a from CourseCatalogModel a where a.campusCode=?1 and a.courseSystem in (?2) and "+
			"(a.groupId in (?3) or (a.groupCode=?4 or a.groupCode like ?4||'/%' or a.groupCode like '%/'||?4||'/%' "+
			"or a.groupCode like '%/'||?4)) and a.code in (?6) and a.status=0 and a.courseId in "+
			"(select b.courseSchedulingPKId.courseId from CourseSchedulingModel b where b.courseSchedulingPKId.semesterSubId=?5 "+ 
			"and b.status=0) order by a.ownerCode, a.code, a.courseVersion")
	List<CourseCatalogModel> findCALSoftSkillCourse(String campusCode, String[] courseSystem, List<Integer> egbGroupId, 
								String groupCode, String semesterSubId, List<String> courseCode);
	
		
	//Soft Skill Courses - FFCS & CAL as List
	@Query("select a from CourseCatalogModel a where a.campusCode=?1 and (a.groupId in (?2) or (a.groupCode=?3 or "+
			"a.groupCode like ?3||'/%' or a.groupCode like '%/'||?3||'/%' or a.groupCode like '%/'||?3)) and "+
			"(a.code in (?5) or a.code in (select c.courseCode from CourseEquivalancesModel c "+
			"where c.equivalentCourseCode in (?5)) or a.code in (select d.equivalentCourseCode "+
			"from CourseEquivalancesModel d where d.courseCode in (?5))) and a.status=0 and a.courseId in "+
			"(select b.courseSchedulingPKId.courseId from CourseSchedulingModel b where b.courseSchedulingPKId.semesterSubId=?4 "+
			"and b.status=0) order by a.ownerCode, a.code, a.courseVersion")
	List<CourseCatalogModel> findFFCSSoftSkillCourse(String campusCode, List<Integer> egbGroupId, String groupCode, 
								String semesterSubId, List<String> courseCode);
}
