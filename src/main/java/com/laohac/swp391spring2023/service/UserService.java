package com.laohac.swp391spring2023.service;

import java.util.List;

import javax.mail.MessagingException;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;

import com.laohac.swp391spring2023.config.CustomOAuth2User;
import com.laohac.swp391spring2023.model.dto.UserDTORequest;
import com.laohac.swp391spring2023.model.dto.UserDTOResponse;
import com.laohac.swp391spring2023.model.dto.UserDTOUpdate;
import com.laohac.swp391spring2023.model.entities.Route;
import com.laohac.swp391spring2023.model.entities.Trip;
import com.laohac.swp391spring2023.model.entities.User;

public interface UserService {

    public UserDTOResponse registerUser(User user);

    public UserDTOResponse login(UserDTORequest userDTORequest);

    public UserDTOResponse getUserInfo(String username);

    public UserDTOResponse authenticated(UserDTORequest userDTORequest);

    public UserDTOResponse update(UserDTOUpdate userUpdate, String username);

    public UserDTOResponse getCurrentUser();

    public boolean checkEmailExisted(String email);

    public void createUserByEmail(CustomOAuth2User oAuth2User);

    public List<Trip> search(Route route);

    public List<Trip> searchByRouteAndDate(Route route, LocalDate date);

    public void sendVerificationEmail(UserDTOResponse user, String siteUrl) throws UnsupportedEncodingException, MessagingException;

    public boolean verify(String code);

}
