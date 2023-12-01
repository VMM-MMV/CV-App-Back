package com.example.demo.services;

import com.example.demo.controller.PersonController;
import com.example.demo.model.Person;
import com.example.demo.repo.PersonRepo;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Service
public class PersonService {
    @Autowired
    PersonRepo repo;

    private static final Logger logger = LoggerFactory.getLogger(PersonService.class);

    public void savePerson(Person person) {
        repo.save(person);
    }

    public List<Person> getAllPersons(){
        return new ArrayList<>(repo.findAll());
    }

    public Person findPersonByEmail(String email) {
        return repo.findByEmail(email);
    }

    public void deletePersonByEmail(Person person) {
        repo.delete(person);
    }

    public void updatePerson(Person previousPerson, Person updatedPerson) {
        Long id = previousPerson.getId();
        if (updatedPerson != null) {
            Field[] fields = Person.class.getDeclaredFields();
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(updatedPerson);
                    if (value != null) {
                        field.set(previousPerson, value);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            previousPerson.setId(id);
            repo.save(previousPerson);
        }
    }
    public void generatePDF(Person person, String pdfFileName) throws FileNotFoundException {
        PdfWriter writer = new PdfWriter(pdfFileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("First Name: "+person.getName() + " " + "Lats Name: " +person.getLastname()));
        document.add(new Paragraph("Date of Birth: " + person.getBirthdate()));
        document.add(new Paragraph("Address: " + person.getAddress() + ", " + person.getCity()));
        document.add(new Paragraph("Email: " + person.getEmail()));
        document.add(new Paragraph("Phone: " + person.getPhoneNumber()));
        document.add(new Paragraph("Nationality: " + person.getNationality()));
        document.add(new Paragraph("Civil Status: " + person.getCivilStatus()));
        document.add(new Paragraph("Sex: " + person.getSex()));
        document.add(new Paragraph("Has Kids: " + person.getHasKids()));

        document.add(new Paragraph("\nEDUCATION"));
        document.add(new Paragraph(person.getEducation() + " from " + person.getSchool()
                + ", " + person.getCitySchool()
                + " (" + person.getStartDateStudy()
                + " to " + person.getEndDateStudy() + ")"));

        document.add(new Paragraph("\nWORK EXPERIENCE"));
        document.add(new Paragraph(person.getTitleJob() + " at " + person.getEmployer()
                + ", " + person.getCityJob()
                + " (" + person.getStartDateJob()
                + " to " + person.getEndDateJob() + ")"));

        org.jsoup.nodes.Document descriptionDoc = Jsoup.parse(person.getDescriptionJob());
        document.add(new Paragraph("Description Job: " + descriptionDoc.text()));

        document.add(new Paragraph("\nSKILLS"));
        document.add(new Paragraph(person.getSkills() + " (" + person.getLevelSkills() + ")"));

        document.add(new Paragraph("\nLANGUAGES"));
        document.add(new Paragraph(person.getLanguage() + " (" + person.getLevelLanguage() + ")"));

        document.add(new Paragraph("\nHOBBIES"));
        document.add(new Paragraph(person.getHobby()));

        org.jsoup.nodes.Document achievementsDoc = Jsoup.parse(person.getAchievements());

        document.add(new Paragraph("\nACHIEVEMENTS"));
        document.add(new Paragraph(achievementsDoc.text()));

        document.close();

        try {
            new ResumeService().uploadResumeToDrive(null, null, "1KFTjVjo4qfR5-HsRQqKSTBk7RKyO5WKe", pdfFileName);
            new ResumeService().deleteDuplicateFilesInDrive();
            File pdfFile = new File(pdfFileName);
            pdfFile.delete();
        } catch (Exception e) {
            logger.error("An error occurred while uploading the PDF to Google Drive: {}", e.getMessage());
        }
    }
}
