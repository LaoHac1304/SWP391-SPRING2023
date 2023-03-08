package com.laohac.swp391spring2023.controller;


import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.laohac.swp391spring2023.model.dto.CheckOutInfoDTOReponse;
import com.laohac.swp391spring2023.model.dto.PaypalUserInfo;
import com.laohac.swp391spring2023.model.entities.OrderDetail;
import com.laohac.swp391spring2023.model.entities.Route;
import com.laohac.swp391spring2023.model.entities.Seat;
import com.laohac.swp391spring2023.model.entities.Trip;
import com.laohac.swp391spring2023.repository.RouteRepository;
import com.laohac.swp391spring2023.repository.SeatRepository;
import com.laohac.swp391spring2023.repository.TripRepository;
import com.laohac.swp391spring2023.service.BookingService;
import com.nimbusds.oauth2.sdk.Request;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class PaymentController {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BookingService bookingService;

    @GetMapping("/{id}")
    public String payment(@PathVariable(value = "id") int id, Model model){

        Trip trip = new Trip();
        
        trip = tripRepository.findById(id);
        model.addAttribute("tripInfoCurrent", trip);
        List<Seat> seatLists = seatRepository.findByTrip(trip);
        
        model.addAttribute("listSeats",seatLists);
        return "home/orderForm";
    }

    @GetMapping("/list-place")
    public String listPlace(Model model) {

        List<Route> listRoute = routeRepository.findAll();
        model.addAttribute("listStates", listRoute);

        Set<String> listState1 = new HashSet<>();
        Set<String> listState2 = new HashSet<>();

        for (Route route : listRoute) {
            listState1.add(route.getDeparture());
            listState2.add(route.getArrival());    
        }

        model.addAttribute("departure", listState1);
        model.addAttribute("arrival", listState2);

        for (Route route : listRoute) {
            System.out.println(route.getArrival());
        }
        return "redirect:/users/search-trip";
    }

    @GetMapping("/detail")
    public String showDetail(){
        return "home/ArrivalDepartureDetail";
    }

    @GetMapping("/checkout/{id}")
    public String showCheckoutForm(Model model, @PathVariable(value = "id") int id, HttpSession session){

        Trip trip = new Trip();
        
        trip = tripRepository.findById(id);
        model.addAttribute("tripInfoCurrent", trip);

        CheckOutInfoDTOReponse checkOutInfoDTOReponse = (CheckOutInfoDTOReponse) session.getAttribute("checkoutinfo");
        model.addAttribute("checkoutinfo", checkOutInfoDTOReponse);
        return "home/checkoutForm";
    }

    @PostMapping("/choose-seats/{id}")
    public String chooseSeats(@RequestParam(name = "selectedSeats", required = false) List<Integer> selectedSeats, 
                                        Model model, @PathVariable(value = "id") int id, HttpSession session){

        for (Integer idd : selectedSeats) {
            bookingService.chooseSeats(idd);    
        }
        model.addAttribute("selectedSeats", selectedSeats);
        CheckOutInfoDTOReponse checkOutInfoDTOReponse = bookingService.showCheckOutInfo(selectedSeats, session);
        
        session.setAttribute("checkoutinfo", checkOutInfoDTOReponse);

       return "redirect:/booking/checkout/{id}";
    }

    private String getSiteURL(HttpServletRequest request){
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }

    @PostMapping("/save-order")
    public String saveOrder(@RequestParam(name = "paymentMethod") String paymentMethod, HttpSession session, Model model, HttpServletRequest request) throws UnsupportedEncodingException, MessagingException{

        
        bookingService.saveOrder(session);
        
        String siteURL = getSiteURL(request);
        bookingService.sendVerificationEmail(session, siteURL);
        if(paymentMethod.equals("paypal")){
            OrderDetail orderDetail = (OrderDetail) session.getAttribute("orderDetailEmail");
            BigDecimal divisor = new BigDecimal("25000");
		    BigDecimal price = orderDetail.getTotal();
            price = price.divide(divisor);
            PaypalUserInfo paypalUserInfo = PaypalUserInfo.builder()
                                            .fullName(orderDetail.getFullName())
                                            .date(new Date())
                                            .price(price)
                                            .build();
            model.addAttribute("paypalUserInfo", paypalUserInfo);
            return "redirect:/";
        }
        return "redirect:/homepage";
    }

    @PostMapping("/cancel-booking")
    public String cancelBooking(@RequestParam("bookingId") int bookingId){
        bookingService.cancelBooking(bookingId);
        return "redirect:/users/info";
    }
}