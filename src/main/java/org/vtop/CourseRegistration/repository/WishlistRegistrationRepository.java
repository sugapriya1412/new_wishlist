package org.vtop.CourseRegistration.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.vtop.CourseRegistration.model.WishlistRegistrationModel;
import org.vtop.CourseRegistration.model.WishlistRegistrationPKModel;


@Repository
public interface WishlistRegistrationRepository extends JpaRepository<WishlistRegistrationModel, WishlistRegistrationPKModel>
{	
	@Query("select count(distinct a.wlRegPKId.courseId) as regcnt from WishlistRegistrationModel a "+
			"where a.wlRegPKId.semesterSubId=?1 and a.wlRegPKId.classGroupId in (?2) and a.wlRegPKId.registerNumber=?3")
	Integer findRegisterNumberTCCount2(String semesterSubId, String[] classGroupId, String registerNumber);
	
	@Modifying
	@Query(value="insert into academics.WISHLIST_REGISTRATION_DELETE (SEMSTR_DETAILS_SEMESTER_SUB_ID, "+
					"CLSSGRP_MASTER_CLASS_GROUP_ID, STDNTSLGNDTLS_REGISTER_NUMBER, COURSE_CATALOG_COURSE_ID, "+ 
					"CRSTYPCMPNTMASTER_COURSE_TYPE, COURSE_OPTION_MASTER_CODE, REGISTRATION_COMPONENT_TYPE, "+ 
					"LOG_USERID, LOG_TIMESTAMP, LOG_IPADDRESS, EQUIVALANCE_COURSE_ID, EQUIVALANCE_COURSE_TYPE, "+
					"EQUIVALANCE_EXAM_MONTH, DELETE_LOG_USERID, DELETE_LOG_TIMESTAMP, DELETE_LOG_IPADDRESS) "+
					"(select SEMSTR_DETAILS_SEMESTER_SUB_ID, CLSSGRP_MASTER_CLASS_GROUP_ID, "+
					"STDNTSLGNDTLS_REGISTER_NUMBER, COURSE_CATALOG_COURSE_ID, CRSTYPCMPNTMASTER_COURSE_TYPE, "+
					"COURSE_OPTION_MASTER_CODE, REGISTRATION_COMPONENT_TYPE, LOG_USERID, LOG_TIMESTAMP, "+
					"LOG_IPADDRESS, EQUIVALANCE_COURSE_ID, EQUIVALANCE_COURSE_TYPE, EQUIVALANCE_EXAM_MONTH, "+
					"?5, CURRENT_TIMESTAMP, ?6 from academics.WISHLIST_REGISTRATION where "+
					"SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and CLSSGRP_MASTER_CLASS_GROUP_ID in (?2) and "+
					"STDNTSLGNDTLS_REGISTER_NUMBER=?3 and COURSE_CATALOG_COURSE_ID=?4)", nativeQuery=true)
	void insertWishlistToDelete(String semesterSubId, String[] classGroupId, String registerNumber, String courseId, 
				String logUserId, String logIpAddress);
	
	@Modifying
	@Query("delete from WishlistRegistrationModel a where a.wlRegPKId.semesterSubId=?1 and "+
			"a.wlRegPKId.classGroupId in (?2) and a.wlRegPKId.registerNumber=?3 and a.wlRegPKId.courseId=?4")
	void deleteByRegisterNumberCourseId(String semesterSubId, String[] classGroupId, 
			String registerNumber, String courseId);
	
	
	@Query("select a from WishlistRegistrationModel a where a.wlRegPKId.semesterSubId=?1 and "+
			"a.wlRegPKId.classGroupId in (?2) and a.wlRegPKId.registerNumber=?3 order by "+
			"a.wlRegPKId.registerNumber, a.wlRegPKId.courseId, a.wlRegPKId.courseType desc")
	List<WishlistRegistrationModel> findByRegisterNumber(String semesterSubId, String[] classGroupId, 
										String registerNumber);
	
	@Query("select a from WishlistRegistrationModel a where a.wlRegPKId.semesterSubId=?1 and "+
			"a.wlRegPKId.classGroupId in (?2) and a.wlRegPKId.registerNumber=?3 and a.wlRegPKId.courseId=?4 "+
			"order by a.wlRegPKId.registerNumber, a.wlRegPKId.courseId, a.wlRegPKId.courseType desc")
	List<WishlistRegistrationModel> findByRegisterNumberCourseId(String semesterSubId, String[] classGroupId, 
											String registerNumber, String courseId);
	
	@Query("select a from WishlistRegistrationModel a where a.wlRegPKId.semesterSubId=?1 and "+
			"a.wlRegPKId.classGroupId in (?2) and a.wlRegPKId.registerNumber=?3 and "+
			"a.courseCatalogModel.code=?4 order by a.wlRegPKId.registerNumber, a.wlRegPKId.courseId, "+
			"a.wlRegPKId.courseType desc")
	List<WishlistRegistrationModel> findByRegisterNumberCourseCode(String semesterSubId, String[] classGroupId, 
									String registerNumber, String courseCode);

	@Query("select count(distinct a.wlRegPKId.courseId) as regcnt from WishlistRegistrationModel a "+
			"where a.wlRegPKId.semesterSubId=?1 and a.wlRegPKId.classGroupId in (?2) and a.wlRegPKId.registerNumber=?3")
	Integer findRegisterNumberTCCount(String semesterSubId, String[] classGroupId, String registerNumber);
	
	@Query(value="select sum(case when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'ETH') then b.LECTURE_CREDITS "+
					"when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'ELA') then b.PRACTICAL_CREDITS "+
					"when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'EPJ') then b.PROJECT_CREDITS else b.CREDITS end) "+
					"as totalCredit from academics.WISHLIST_REGISTRATION a, academics.COURSE_CATALOG b where "+
					"a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and a.CLSSGRP_MASTER_CLASS_GROUP_ID in (?2) and "+
					"a.STDNTSLGNDTLS_REGISTER_NUMBER=?3 and a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID", nativeQuery=true)
	Integer findRegisterNumberTotalCredits(String semesterSubId, String[] classGroupId, String registerNumber);
	
	
	/*@Query(value="select a.SEMSTR_DETAILS_SEMESTER_SUB_ID, a.COURSE_CATALOG_COURSE_ID, a.course_code, a.course_title,  "+ 
					"a.GENERIC_COURSE_TYPE, a.generic_course_type_desc, a.COURSE_OPTION_MASTER_CODE, sum(a.course_type_credit) "+ 
					"as credit from "+ 
					"(select a.SEMSTR_DETAILS_SEMESTER_SUB_ID, a.COURSE_CATALOG_COURSE_ID, b.CODE as course_code, "+ 
					"b.TITLE as course_title, b.GENERIC_COURSE_TYPE, c.DESCRIPTION as generic_course_type_desc, "+ 
					"a.COURSE_OPTION_MASTER_CODE, a.CRSTYPCMPNTMASTER_COURSE_TYPE, "+ 
					"(case when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'ETH') then b.LECTURE_CREDITS "+ 
					"when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'ELA') then b.PRACTICAL_CREDITS "+ 
					"when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'EPJ') then b.PROJECT_CREDITS "+ 
					"when ((a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'OC') and (a.COURSE_OPTION_MASTER_CODE = 'RGR')) then 0 "+ 
					"else b.CREDITS end) as course_type_credit from ACADEMICS.WISHLIST_REGISTRATION a, "+ 
					"ACADEMICS.COURSE_CATALOG b, ACADEMICS.COURSE_TYPE_COMPONENT_MASTER c where "+ 
					"a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and a.CLSSGRP_MASTER_CLASS_GROUP_ID in (?2) and "+ 
					"a.STDNTSLGNDTLS_REGISTER_NUMBER in (?3) and a.COURSE_OPTION_MASTER_CODE in (?4) and "+ 
					"a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID and b.GENERIC_COURSE_TYPE=c.COURSE_TYPE) a "+ 
					"group by a.SEMSTR_DETAILS_SEMESTER_SUB_ID, a.COURSE_CATALOG_COURSE_ID, a.course_code, a.course_title, "+ 
					"a.GENERIC_COURSE_TYPE, a.generic_course_type_desc, a.COURSE_OPTION_MASTER_CODE  "+ 
					"order by a.SEMSTR_DETAILS_SEMESTER_SUB_ID, a.course_code", nativeQuery=true)
	List<Object[]> findByClassGroupRegisterNumberAndCourseOption(String semesterSubId, List<String> classGroupId, 
						List<String> registerNumber, List<String> courseOptionCode);*/
	@Query(value="select a.SEMSTR_DETAILS_SEMESTER_SUB_ID, a.COURSE_CATALOG_COURSE_ID, a.course_code, a.course_title,  "+ 
					"a.GENERIC_COURSE_TYPE, a.generic_course_type_desc, a.COURSE_OPTION_MASTER_CODE, a.CURRICULUM_COURSE_CATEGORY, "+
					"sum(a.course_type_credit) as credit from "+ 
					"(select a.SEMSTR_DETAILS_SEMESTER_SUB_ID, a.COURSE_CATALOG_COURSE_ID, b.CODE as course_code, "+ 
					"b.TITLE as course_title, b.GENERIC_COURSE_TYPE, c.DESCRIPTION as generic_course_type_desc, "+ 
					"a.COURSE_OPTION_MASTER_CODE, a.CRSTYPCMPNTMASTER_COURSE_TYPE, d.CURRICULUM_COURSE_CATEGORY, "+ 
					"(case when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'ETH') then b.LECTURE_CREDITS "+ 
					"when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'ELA') then b.PRACTICAL_CREDITS "+ 
					"when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'EPJ') then b.PROJECT_CREDITS "+ 
					"when ((a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'OC') and (a.COURSE_OPTION_MASTER_CODE = 'RGR')) then 0 "+ 
					"else b.CREDITS end) as course_type_credit from ACADEMICS.WISHLIST_REGISTRATION a, "+ 
					"ACADEMICS.COURSE_CATALOG b, ACADEMICS.COURSE_TYPE_COMPONENT_MASTER c, ACADEMICS.COURSE_OPTION_MASTER d where "+ 
					"a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and a.CLSSGRP_MASTER_CLASS_GROUP_ID in (?2) and "+ 
					"a.STDNTSLGNDTLS_REGISTER_NUMBER in (?3) and a.COURSE_OPTION_MASTER_CODE in (?4) and "+ 
					"a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID and b.GENERIC_COURSE_TYPE=c.COURSE_TYPE and "+
					"a.COURSE_OPTION_MASTER_CODE=d.CODE) a group by a.SEMSTR_DETAILS_SEMESTER_SUB_ID, a.COURSE_CATALOG_COURSE_ID, "+
					"a.course_code, a.course_title, a.GENERIC_COURSE_TYPE, a.generic_course_type_desc, a.COURSE_OPTION_MASTER_CODE, "+
					"a.CURRICULUM_COURSE_CATEGORY order by a.SEMSTR_DETAILS_SEMESTER_SUB_ID, a.course_code", nativeQuery=true)
	List<Object[]> findByClassGroupRegisterNumberAndCourseOption(String semesterSubId, List<String> classGroupId, 
						List<String> registerNumber, List<String> courseOptionCode);

	
	@Query(value="SELECT a.COURSE_CATALOG_COURSE_ID, b.CODE, b.TITLE, b.GENERIC_COURSE_TYPE, a.CRSTYPCMPNTMASTER_COURSE_TYPE, "+
					"c.DESCRIPTION as COURSE_TYPE_DESCRIPTION, (case when ((a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'ETH') or "+
					"(a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'TH') or (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'SS')) then b.LECTURE_HOURS "+
					"else 0 end) as LECTURE_HOUR, (case when ((a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'ETH') or "+
					"(a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'TH')) then b.TUTORIAL_HOURS else 0 end) as TUTORIAL_HOUR, "+
					"(case when ((a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'ELA') or (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'LO') or "+
					"(a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'SS')) then b.PRACTICAL_HOURS else 0 end) as PRACTICAL_HOUR, "+
					"(case when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'EPJ') then b.PROJECT_HOURS else 0 end) as PROJECT_HOUR, "+
					"(case when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'ETH') then b.LECTURE_CREDITS "+
					"when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'ELA') then b.PRACTICAL_CREDITS "+
					"when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'EPJ') then b.PROJECT_CREDITS else b.CREDITS end) as CREDIT, "+
					"a.COURSE_OPTION_MASTER_CODE, d.DESCRIPTION as COURSE_OPTION_DESCRIPTION, b.EVALUATION_TYPE, "+
					"(case when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'ETH') then 2 when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'ELA') "+
					"then 3 when (a.CRSTYPCMPNTMASTER_COURSE_TYPE = 'EPJ') then 4 else 1 end) as COURSE_TYPE_ORDER "+
					"FROM ACADEMICS.WISHLIST_REGISTRATION a, ACADEMICS.COURSE_CATALOG b, ACADEMICS.COURSE_TYPE_COMPONENT_MASTER c, "+
					"ACADEMICS.COURSE_OPTION_MASTER d where a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and a.CLSSGRP_MASTER_CLASS_GROUP_ID "+
					"in (?2) and a.STDNTSLGNDTLS_REGISTER_NUMBER=?3 and a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID and "+
					"a.CRSTYPCMPNTMASTER_COURSE_TYPE=c.COURSE_TYPE and a.COURSE_OPTION_MASTER_CODE=d.CODE "+
					"order by a.COURSE_CATALOG_COURSE_ID, COURSE_TYPE_ORDER", nativeQuery=true)
	List<Object[]> findByRegisterNumberAsObject(String semesterSubId, String[] classGroupId, String registerNumber);
	
	@Query("select distinct a.wlRegPKId.courseId from WishlistRegistrationModel a where a.wlRegPKId.semesterSubId=?1 "+
			"and a.wlRegPKId.classGroupId in (?2) and a.wlRegPKId.registerNumber=?3 order by a.wlRegPKId.courseId")
	List<String> findCourseByRegisterNumberAndClassGroup(String semesterSubId, String[] classGroupId, String registerNumber);
	
	@Query(value="select distinct b.CODE as course_code, b.TITLE as course_title, b.GENERIC_COURSE_TYPE from "+
					"ACADEMICS.WISHLIST_REGISTRATION a, ACADEMICS.COURSE_CATALOG b where "+
					"a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and a.CLSSGRP_MASTER_CLASS_GROUP_ID in (?2) and "+
					"a.STDNTSLGNDTLS_REGISTER_NUMBER=?3 and a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID and "+
					"b.CODE=?4", nativeQuery=true)
	List<Object[]> findWLRegistrationByClassGroupAndCourseCode(String semesterSubId, String[] classGroupId, 
						String registerNumber, String courseCode);

	@Query(value="select distinct b.CODE as course_code, b.TITLE as course_title, b.GENERIC_COURSE_TYPE from "+
					"ACADEMICS.WISHLIST_REGISTRATION a, ACADEMICS.COURSE_CATALOG b where "+
					"a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and a.CLSSGRP_MASTER_CLASS_GROUP_ID in (?2) and "+
					"a.STDNTSLGNDTLS_REGISTER_NUMBER=?3 and a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID and "+
					"(b.CODE in (select EQUIVALENT_COURSE_CODE from ACADEMICS.COURSE_EQUIVALANCES where "+
					"COURSE_CODE=?4) or b.CODE in (select COURSE_CODE from ACADEMICS.COURSE_EQUIVALANCES where "+
					"EQUIVALENT_COURSE_CODE=?4))", nativeQuery=true)
	List<Object[]> findWLCERegistrationByClassGroupAndCourseCode(String semesterSubId, String[] classGroupId, 
						String registerNumber, String courseCode);
	
	@Query("select count(distinct a.wlRegPKId.courseId) as courseCount from WishlistRegistrationModel a "+
			"where a.wlRegPKId.semesterSubId=?1 and a.wlRegPKId.classGroupId in (?2) and a.wlRegPKId.registerNumber=?3 "+
			"and a.courseOptionCode in (?4)")
	Integer findCourseCountByRegisterNumberAndCourseOption(String semesterSubId, String[] classGroupId, 
					String registerNumber, List<String> courseOption);
		
	@Query("select distinct a.courseCatalogModel.code from WishlistRegistrationModel a where a.wlRegPKId.semesterSubId=?1 and "+
			"a.wlRegPKId.classGroupId in (?2) and a.wlRegPKId.registerNumber in (?3) order by a.courseCatalogModel.code")
	List<String> findRegisteredCourseByRegisterNumber(String semesterSubId, String[] classGroupId, List<String> registerNumber);
	
	@Query("select distinct b.code from WishlistRegistrationModel a, CourseCatalogModel b where a.wlRegPKId.semesterSubId=?1 and "+
			"a.wlRegPKId.classGroupId in (?2) and a.wlRegPKId.registerNumber in (?3) and a.equivalanceCourseId=b.courseId "+
			"order by b.code")
	List<String> findEquivalanceRegisteredCourseByRegisterNumber(String semesterSubId, String[] classGroupId, List<String> registerNumber);
	
	@Query(value="select distinct b.CODE as course_code, b.TITLE as course_title, b.GENERIC_COURSE_TYPE, "+
					"a.COURSE_OPTION_MASTER_CODE from ACADEMICS.WISHLIST_REGISTRATION a, "+
					"ACADEMICS.COURSE_CATALOG b where a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and "+
					"a.CLSSGRP_MASTER_CLASS_GROUP_ID in (?2) and a.STDNTSLGNDTLS_REGISTER_NUMBER=?3 and "+
					"a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID and "+
					"(b.CODE=?4 or b.CODE in (select EQUIVALENT_COURSE_CODE from ACADEMICS.COURSE_EQUIVALANCES "+
					"where COURSE_CODE=?4) or b.CODE in (select COURSE_CODE from ACADEMICS.COURSE_EQUIVALANCES "+
					"where EQUIVALENT_COURSE_CODE=?4)) order by course_code", nativeQuery=true)
	List<Object[]> findCourseOptionByRegisterNumberAndCourseCode(String semesterSubId, String[] classGroupId, 
						String registerNumber, String courseCode);
		
	@Query(value="select distinct a.course_code from ("+
					"(select b.CODE as course_code from ACADEMICS.WISHLIST_REGISTRATION a, ACADEMICS.COURSE_CATALOG b "+
					"where a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and a.CLSSGRP_MASTER_CLASS_GROUP_ID in (?2) and "+
					"a.STDNTSLGNDTLS_REGISTER_NUMBER=?3 and a.COURSE_OPTION_MASTER_CODE in (?4) and "+
					"a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID) "+
					"union all "+
					"(select EQUIVALENT_COURSE_CODE as course_code from ACADEMICS.COURSE_EQUIVALANCES where COURSE_CODE "+
					"in (select distinct b.CODE from ACADEMICS.WISHLIST_REGISTRATION a, ACADEMICS.COURSE_CATALOG b "+
					"where a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and a.CLSSGRP_MASTER_CLASS_GROUP_ID in (?2) and "+
					"a.STDNTSLGNDTLS_REGISTER_NUMBER=?3 and a.COURSE_OPTION_MASTER_CODE in (?4) and "+
					"a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID)) "+
					"union all "+
					"(select COURSE_CODE as course_code from ACADEMICS.COURSE_EQUIVALANCES where EQUIVALENT_COURSE_CODE "+
					"in (select distinct b.CODE from ACADEMICS.WISHLIST_REGISTRATION a, ACADEMICS.COURSE_CATALOG b "+
					"where a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and a.CLSSGRP_MASTER_CLASS_GROUP_ID in (?2) and "+
					"a.STDNTSLGNDTLS_REGISTER_NUMBER=?3 and a.COURSE_OPTION_MASTER_CODE in (?4) and "+
					"a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID)) "+
					") a order by a.course_code", nativeQuery=true)
	List<String> findUEWishlistCourseByRegisterNumberAndCourseOption(String semesterSubId, String[] classGroupId, 
						String registerNumber, List<String> courseOption);
	
	@Query(value="select distinct a.COURSE_CATALOG_COURSE_ID from ACADEMICS.WISHLIST_REGISTRATION a, "+
					"ACADEMICS.COURSE_CATALOG b where a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and "+
					"a.CLSSGRP_MASTER_CLASS_GROUP_ID in (?2) and a.STDNTSLGNDTLS_REGISTER_NUMBER=?3 and "+
					"a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID and (substr(b.CODE,1,3) in ('SET') or "+
					"(a.COURSE_OPTION_MASTER_CODE in ('RGR') and b.GENERIC_COURSE_TYPE in ('SS'))) "+
					"order by a.COURSE_CATALOG_COURSE_ID", nativeQuery=true)
	List<String> findBlockedCourseIdByRegisterNumberForDelete(String semesterSubId, String[] classGroupId, 
					String registerNumber);
	
	@Query(value="select a.order_no, a.reg_type, a.course_code, a.course_title, a.GENERIC_COURSE_TYPE from ("+ 
					"(select 1 as order_no, 'REG' as reg_type, b.CODE as course_code, b.TITLE as course_title, b.GENERIC_COURSE_TYPE "+ 
					"from ACADEMICS.WISHLIST_REGISTRATION a, ACADEMICS.COURSE_CATALOG b where a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 "+ 
					"and a.CLSSGRP_MASTER_CLASS_GROUP_ID in (?2) and a.STDNTSLGNDTLS_REGISTER_NUMBER=?3 and "+ 
					"a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID and b.CODE=?4) "+ 
					"union all "+ 
					"(select 2 as order_no, 'CEREG' as reg_type, b.CODE as course_code, b.TITLE as course_title, b.GENERIC_COURSE_TYPE "+ 
					"from ACADEMICS.WISHLIST_REGISTRATION a, ACADEMICS.COURSE_CATALOG b where a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 "+ 
					"and a.CLSSGRP_MASTER_CLASS_GROUP_ID in (?2) and a.STDNTSLGNDTLS_REGISTER_NUMBER=?3 and "+ 
					"a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID  and (b.CODE in (select EQUIVALENT_COURSE_CODE from ACADEMICS.COURSE_EQUIVALANCES "+ 
					"where COURSE_CODE=?4) or b.CODE in (select COURSE_CODE from ACADEMICS.COURSE_EQUIVALANCES where "+ 
					"EQUIVALENT_COURSE_CODE=?4)))) a order by a.order_no", nativeQuery=true)
	List<Object[]> findWLRegistrationWithCEByClassGroupAndCourseCode(String semesterSubId, String[] classGroupId, String registerNumber, 
						String courseCode);
	
	
	@Query(value="select a.course_code from ("+
					"(select b.CODE as course_code from ACADEMICS.WISHLIST_REGISTRATION a, ACADEMICS.COURSE_CATALOG b where "+
					"a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and a.CLSSGRP_MASTER_CLASS_GROUP_ID in (?3) and "+
					"a.STDNTSLGNDTLS_REGISTER_NUMBER in (?2) and a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID) "+
					"union all "+
					"(select b.CODE as course_code from ACADEMICS.WISHLIST_REGISTRATION a, ACADEMICS.COURSE_CATALOG b where "+
					"a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and a.CLSSGRP_MASTER_CLASS_GROUP_ID in (?3) and "+
					"a.STDNTSLGNDTLS_REGISTER_NUMBER in (?2) and a.EQUIVALANCE_COURSE_ID=b.COURSE_ID) "+
					"union all "+
					"(select b.CODE as course_code from ACADEMICS.COURSE_REGISTRATION a, ACADEMICS.COURSE_CATALOG b where "+
					"a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and a.STDNTSLGNDTLS_REGISTER_NUMBER in (?2) and "+
					"a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID) "+
					"union all "+
					"(select b.CODE as course_code from ACADEMICS.COURSE_REGISTRATION_WAITING a, ACADEMICS.COURSE_CATALOG b "+ 
					"where a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and a.STDNTSLGNDTLS_REGISTER_NUMBER in (?2) "+
					"and a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID) "+
					"union all "+
					"(select (case when (b.CODE is null) then a.EQUIVALANCE_COURSE_ID else b.CODE end) as course_code "+
					"from ACADEMICS.COURSE_EQUIVALANCE_REG a left outer join (select COURSE_ID, CODE from "+
					"ACADEMICS.COURSE_CATALOG) b on (b.COURSE_ID=a.EQUIVALANCE_COURSE_ID) where "+
					"a.SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and a.STDNTSLGNDTLS_REGISTER_NUMBER in (?2) and "+
					"a.EQUIVALANCE_COURSE_ID is not null) "+
					"union all "+
					"(select COURSE_CODE from ACADEMICS.STUDENT_HISTORY where STDNTSLGNDTLS_REGISTER_NUMBER in (?2) "+
					"and COURSE_CATALOG_COURSE_ID in (select EQUIVALANCE_COURSE_ID from ACADEMICS.COURSE_EQUIVALANCE_REG "+
					"where SEMSTR_DETAILS_SEMESTER_SUB_ID=?1 and STDNTSLGNDTLS_REGISTER_NUMBER in (?2))) "+
					"union all "+
					"(select b.CODE as course_code from ACADEMICS.COURSE_REGISTRATION a, ACADEMICS.COURSE_CATALOG b, "+
					"academics.COURSE_REG_PREVIOUS_SEM_VIEW2 c where a.SEMSTR_DETAILS_SEMESTER_SUB_ID=c.SEMESTER_SUB_ID and "+
					"a.STDNTSLGNDTLS_REGISTER_NUMBER in (?2) and a.result_status in (0,1) and a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID) "+
					"union all "+
					"(select b.CODE as course_code from ACADEMICS.COURSE_REGISTRATION a, ACADEMICS.COURSE_CATALOG b, "+
					"academics.COURSE_REG_PREVIOUS_SEM_VIEW2 c, ACADEMICS.COURSE_EQUIVALANCES d where "+
					"a.SEMSTR_DETAILS_SEMESTER_SUB_ID=c.SEMESTER_SUB_ID and a.STDNTSLGNDTLS_REGISTER_NUMBER in (?2) and "+
					"a.result_status in (0,1) and a.COURSE_CATALOG_COURSE_ID=b.COURSE_ID and "+
					"(b.CODE=d.COURSE_CODE or b.CODE=d.EQUIVALENT_COURSE_CODE)) "+
					") a order by a.course_code", nativeQuery=true)
	List<String> findCourseFromWLRegistrationAndStudentHistoryBySemesterAndRegisterNoforCS(String semesterSubId, List<String> registerNumber, 
						String[] classGroupId);
}
