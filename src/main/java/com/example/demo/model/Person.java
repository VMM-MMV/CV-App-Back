package com.example.demo.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.YearMonth;

@Data
@Entity
@Table(name = "personal_info")
@SecondaryTable(name = "education_info", pkJoinColumns = @PrimaryKeyJoinColumn(name = "person_id"))
@SecondaryTable(name = "job_history", pkJoinColumns = @PrimaryKeyJoinColumn(name = "person_id"))
@SecondaryTable(name = "skills_info", pkJoinColumns = @PrimaryKeyJoinColumn(name = "person_id"))
@SecondaryTable(name = "language_info", pkJoinColumns = @PrimaryKeyJoinColumn(name = "person_id"))
@SecondaryTable(name = "phone_number", pkJoinColumns = @PrimaryKeyJoinColumn(name = "person_id"))
public class Person {
    public enum Sex {
        MALE, FEMALE
    }

    public enum Kids {
        YES, NO
    }
    public enum CivilStatus{
        Unmarried, Married, Divorced, Widowed
    }
    public enum LevelSkills{
        Beginner, Moderate, Skillful, Experienced, Expert
    }
    public enum LevelLanguage{
        A1, A2, B1, B2, C1, C2, nativeSpeaker, workingKnowledge
    }
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "person_id_seq")
    @SequenceGenerator(name = "person_id_seq", sequenceName = "person_id_seq", allocationSize = 1, initialValue = 1000)
    private long id;
    @Column(length = 25)
    private String name;
    @Column(length = 25)
    private String lastname;
    private LocalDate birthdate;
    @Column(length = 60)
    private String city;
    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Enumerated(EnumType.STRING)
    private Kids hasKids;

    @Column(unique = true, length = 50)
    private String email;

    @Column(table = "phone_number", length = 5)
    private String countryCode;

    @Column(table = "phone_number", length = 15)
    private String phoneNumber;

    @Column(length = 60)
    private String address;

    @Column(length = 20)
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private CivilStatus civilStatus;

    @Column(table = "education_info", length = 60)
    private String education;

    @Column(table = "education_info", length = 60)
    private String school;

    @Column(table = "education_info", length = 60)
    private String citySchool;

    @Column(table = "education_info")
    private LocalDate startDateStudy;

    @Column(table = "education_info")
    private LocalDate endDateStudy;

    @Column(table = "job_history", length = 30)
    private String titleJob;

    @Column(table = "job_history", length = 30)
    private String employer;

    @Column(table = "job_history", length = 60)
    private String cityJob;

    @Column(table = "job_history")
    private LocalDate startDateJob;

    @Column(table = "job_history")
    private LocalDate endDateJob;

    @Column(table = "job_history", columnDefinition = "text")
    private String descriptionJob;

    @Column(table = "skills_info", length = 50)
    private String skills;

    @Column(table = "skills_info", length = 50)
    private LevelSkills levelSkills;

    @Column(table = "language_info", length = 50)
    private String language;

    @Column(table = "language_info", length = 50)
    private LevelLanguage levelLanguage;

    private String hobby;
    @Column(columnDefinition = "text")
    private String achievements;
}
