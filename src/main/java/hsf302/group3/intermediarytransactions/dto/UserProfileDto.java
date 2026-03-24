package hsf302.group3.intermediarytransactions.dto;

import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.entity.UserProfile;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileDto {
    private String fullname;
    private String phone;
    private String email;
    private LocalDate dateOfBirth;
    private String avatar;
    private UserProfile.Gender gender;
    private String description;

    public UserProfileDto(User user) {
        if (user.getProfile() != null) {
            this.fullname = user.getProfile().getFullname();
            this.phone = user.getProfile().getPhone();
            this.email = user.getProfile().getEmail();
            this.dateOfBirth = user.getProfile().getDateOfBirth();
            this.avatar = user.getProfile().getAvatar();
            this.gender = user.getProfile().getGender();
            this.description = user.getProfile().getDescription();
        }
    }
    public UserProfileDto(){}
}