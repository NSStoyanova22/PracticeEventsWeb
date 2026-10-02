package com.example.hefest.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Името е задължително.")
    private String name;

    @NotBlank(message = "Имейлът е задължителен.")
    @Email(message = "Въведете валиден имейл.")
    private String email;

    @NotBlank(message = "Паролата е задължителна.")
    @Size(min = 6, message = "Паролата трябва да е поне 6 символа.")
    private String password;

    @NotBlank(message = "Потвърждението на паролата е задължително.")
    private String passwordConfirmation;

    @NotBlank(message = "Телефонният номер е задължителен.")
    private String phone;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPasswordConfirmation() { return passwordConfirmation; }
    public void setPasswordConfirmation(String passwordConfirmation) { this.passwordConfirmation = passwordConfirmation; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
