package com.mysite.sbb.question;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Setter
@Getter
public class QuestionDTO {
    private Integer id;
    private String subject;
    private String content;
    private String createDate;

    // DateTimeFormatter as a constant
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    public QuestionDTO(Integer id, String subject, String content, LocalDateTime createDate) {
        this.id = id;
        this.subject = subject;
        this.content = content;
        this.setCreateDate(createDate); // Use setter to format date
    }

    public void setCreateDate(LocalDateTime createDate) {
        if (createDate != null) {
            this.createDate = createDate.format(FORMATTER);
        } else {
            this.createDate = null;
        }
    }
}
