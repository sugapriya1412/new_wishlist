package org.vtop.CoureRegistration.mongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection="StudentDetailOthers")
public class StudentDetailOthers
{
	@Id
	private String id;
	
	private String registerNumber;
	private String oldRegisterNumber;
	private int	examGraduationStatus;
	private int	registrationExcemptionStatus;
	private float totalCreditRegistered;
	private float totalCreditEarned;
	private float cumulativeGradePointAverage;
	private Integer	eptMark;
	private Integer	sptMark;
	private String hscGroup;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRegisterNumber() {
		return registerNumber;
	}
	public void setRegisterNumber(String registerNumber) {
		this.registerNumber = registerNumber;
	}
	public String getOldRegisterNumber() {
		return oldRegisterNumber;
	}
	public void setOldRegisterNumber(String oldRegisterNumber) {
		this.oldRegisterNumber = oldRegisterNumber;
	}
	public int getExamGraduationStatus() {
		return examGraduationStatus;
	}
	public void setExamGraduationStatus(int examGraduationStatus) {
		this.examGraduationStatus = examGraduationStatus;
	}
	public int getRegistrationExcemptionStatus() {
		return registrationExcemptionStatus;
	}
	public void setRegistrationExcemptionStatus(int registrationExcemptionStatus) {
		this.registrationExcemptionStatus = registrationExcemptionStatus;
	}
	public float getTotalCreditRegistered() {
		return totalCreditRegistered;
	}
	public void setTotalCreditRegistered(float totalCreditRegistered) {
		this.totalCreditRegistered = totalCreditRegistered;
	}
	public float getTotalCreditEarned() {
		return totalCreditEarned;
	}
	public void setTotalCreditEarned(float totalCreditEarned) {
		this.totalCreditEarned = totalCreditEarned;
	}
	public float getCumulativeGradePointAverage() {
		return cumulativeGradePointAverage;
	}
	public void setCumulativeGradePointAverage(float cumulativeGradePointAverage) {
		this.cumulativeGradePointAverage = cumulativeGradePointAverage;
	}
	public Integer getEptMark() {
		return eptMark;
	}
	public void setEptMark(Integer eptMark) {
		this.eptMark = eptMark;
	}
	public Integer getSptMark() {
		return sptMark;
	}
	public void setSptMark(Integer sptMark) {
		this.sptMark = sptMark;
	}
	public String getHscGroup() {
		return hscGroup;
	}
	public void setHscGroup(String hscGroup) {
		this.hscGroup = hscGroup;
	}
	
	@Override
	public String toString() {
		return "StudentDetailOthers [id=" + id + ", registerNumber=" + registerNumber + ", oldRegisterNumber="
				+ oldRegisterNumber + ", examGraduationStatus=" + examGraduationStatus
				+ ", registrationExcemptionStatus=" + registrationExcemptionStatus + ", totalCreditRegistered="
				+ totalCreditRegistered + ", totalCreditEarned=" + totalCreditEarned + ", cumulativeGradePointAverage="
				+ cumulativeGradePointAverage + ", eptMark=" + eptMark + ", sptMark=" + sptMark + ", hscGroup="
				+ hscGroup + "]";
	}
}
