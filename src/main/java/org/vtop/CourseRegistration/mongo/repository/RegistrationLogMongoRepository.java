package org.vtop.CourseRegistration.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.vtop.CoureRegistration.mongo.model.RegistrationLog;


@Repository
public interface RegistrationLogMongoRepository extends MongoRepository<RegistrationLog, String>
{
	@Query(value="{'registerNumber':?0}")
	RegistrationLog findByRegisterNumber(String registerNumber);
}
