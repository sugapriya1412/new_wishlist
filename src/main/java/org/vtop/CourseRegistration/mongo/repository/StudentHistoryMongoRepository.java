package org.vtop.CourseRegistration.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.vtop.CoureRegistration.mongo.model.StudentHistory;


public interface StudentHistoryMongoRepository extends MongoRepository<StudentHistory, String>
{
	@Query(value="{'registerNumber' : {$in : ?0}}")
	List<StudentHistory> findByRegisterNumber(List<String> registerNumber);
	
	@Query(value="{'registerNumber' : {$in : ?0}, 'grade' : {$nin : ?1}}")
	List<StudentHistory> findFailedCourseByRegisterNumber(List<String> registerNumber, List<String> notGrade);
	
	@Query(value="{'registerNumber' : ?0}", delete = true)
	public void deleteByRegisterNumber(String registerNumber);
}
