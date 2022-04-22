package com.SkhuMentoring.controller;


import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import com.SkhuMentoring.service.UserService;

import com.SkhuMentoring.dto.*;
import com.SkhuMentoring.mapper.MentoringBoardMapper;
import com.SkhuMentoring.mapper.MyPageMapper;
import com.SkhuMentoring.mapper.UserMapper;

import com.SkhuMentoring.serivce.MailService;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/")
public class HomeController {

    private final MentoringBoardMapper mentoringBoardMapper;
    private final MyPageMapper myPageMapper;
    private final UserMapper userMapper;


    private  final MailService mailService;


    @GetMapping("/")
    public String main() {
        return "index";
    }

    /*@PostMapping("/login")
    public String login2(User user, HttpSession session, Model model) {
        log.info(user);
        if(userMapper.login(user)==1){
            session.setAttribute("sessionId",userMapper.getId(user.getUserId()));
            model.addAttribute("sessionId", session.getAttribute("sessionId"));
            return "index";
        }
        else return "login";
    }*/
    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }
    @GetMapping("/menteeStatus")
    public String menteeStatus(Model model) {
        model.addAttribute("list",myPageMapper.getMenteeStatus());
        return  "menteeStatus";
    }

    @GetMapping("/mentorStatus")
    public String mentorStatus(Model model) {
        model.addAttribute("list" ,myPageMapper.getMentorStatus());
        return  "mentorStatus";
    }

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {return  "login";}

    @PostMapping("/login")
    public String login(@RequestParam Map<String, String> map, Model model, HttpSession session) {
        try {
            if (map.get("userId") == null || map.get("userPw") == null) {
                model.addAttribute("msg", "아이디 또는 비밀번호를 입력해주세요");
                return "login";
            }
            User user = userService.login(map);
            if (user != null) {
                session.setAttribute("user", user);
            } else {
                model.addAttribute("msg", "아이디 또는 비밀번호가 올바르지 않습니다.");
                return "login";
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("msg", "로그인 중 문제가 발생했습니다.");
            return "login";
        }
        return "index";
    } // end of PostMapping("login")


    @GetMapping("/myInfo")
    public String myInfo() {return  "myInfo";}

    @GetMapping("/findPwId")
    public String findPwId() {return  "findPwId";}



    @GetMapping("/ranking")
    public String ranking() {return  "ranking";}



    @GetMapping("/mentorRegister") // 멘토 게시글 등록페이지로 이동
    public String MentoRegister(Department department, Subject subject, Model model, @ModelAttribute Mentor mentor)  {
        model.addAttribute("departments" , mentoringBoardMapper.getDepartment());
        model.addAttribute("subject", mentoringBoardMapper.getSubject());
        model.addAttribute("menteeStudentNum",mentor.getMenteeStudentNum());
        /*mentoringBoardMapper.setUpMentoring(menteeStudentNum);*/
        return  "mentorRegister";
    }

    @Transactional // 멘토 게시글 등록
    @PostMapping("/mentorRegister")
    public String MentoRegister(@ModelAttribute Mentor mentor, @ModelAttribute Department department,HttpSession session){
        if(mentor.getSubjectName()=="기타" || mentor.getSubjectName().equals("기타")){
            mentor.setSubjectName(mentor.getAddSubject());
            Subject subject = new Subject();
            subject.setSubjectName(mentor.getSubjectName());
            mentoringBoardMapper.insertSubject(subject); // 기타항목 선택 후 입력한 강의 DB에 등록
        }
        mentor.setUserId((String)session.getAttribute("sessionId"));
        mentoringBoardMapper.insertMentorBoard(mentor);
        return "redirect:/mentorStatus";
    }

    @GetMapping("/menteeRegister")
    public String MenteeRegister(@ModelAttribute Mentee mentee, Model model, Department department){
        model.addAttribute("departments", mentoringBoardMapper.getDepartment());
        model.addAttribute("subject" , mentoringBoardMapper.getSubject());
        return "menteeRegister";
    }
    @Transactional
    @PostMapping("/menteeRegister") // 멘티 게시글 등록
    public String MenteeRegister(@ModelAttribute Mentee mentee, HttpSession session ){
        if(mentee.getSubjectName()=="기타" || mentee.getSubjectName().equals("기타")){
            mentee.setSubjectName(mentee.getAddSubject());
            Subject subject = new Subject();
            subject.setSubjectName(mentee.getSubjectName());
            mentoringBoardMapper.insertSubject(subject); // 기타항목 선택 후 입력한 강의 DB에 등록된다
        }
        mentee.setUserId((String) session.getAttribute("sessionId"));
        mentoringBoardMapper.insertMenteeBoard(mentee);
        return "redirect:/menteeStatus";
    }

    @GetMapping(value = "/checkSubject") // 멘토 , 멘티 게시글 등록 시 기타 항목 과목 기입 후 중복확인 
    @ResponseBody
    public String userIdCheck(String subject) throws Exception {

        int result = mentoringBoardMapper.checkSubject(subject);
        if (result == 1) { // result로 받은 값이 1이라면 이미 등록된 과목으로 fail 리턴
            return "fail";
        }
        else { // result 값이 1이 아니라면 없는 아이디로 success 리턴
            return "success";  }
    }
    //마이페이지로 이동
    @GetMapping("/myPage")
    public String myPageGo(@ModelAttribute Mentor mentor, Model model){
        model.addAttribute("list",myPageMapper.getMentorStatus());
        model.addAttribute("list2",myPageMapper.getMenteeStatus());
        return "myPage";
    }
    //마이페이지 > 멘토 현황 > 멘토링 종료
    @GetMapping("/endMentoring")
    public String endMentoring(@RequestParam("subjectName") String subjectName,Model model, HttpServletRequest req, HttpServletResponse resp){
        log.info(subjectName);
        myPageMapper.endMentoring(subjectName);
        String referer = req.getHeader("Referer");
        return "redirect:"+ referer;
    }
    // 마이페이지 > 멘토 현황 > 상세보기
    @GetMapping("/detailMentoring")
    public String detailMentee(Model model){
        model.addAttribute("list2",myPageMapper.getMenteeStatus());
        return "detailMentoring";
    }
    // 마이페이지 > 멘티에게 받은 요청 > 거절하기
    @GetMapping("/requestRefusal")
    public String requestRefusal(@RequestParam("userStudentNum") String userStudentNum,Model model, HttpServletRequest req, HttpServletResponse resp){
        log.info(userStudentNum);
        myPageMapper.requestRefusal(userStudentNum);
        String referer = req.getHeader("Referer");
        return "redirect:"+ referer;
    }
    // 마이페이지 > 멘티에게 받은 요청 > 수락
    @GetMapping("/requestAccept")
    public String requestAccept(@RequestParam("userStudentNum") String userStudentNum, Model model, HttpServletRequest req, HttpServletResponse resp) {
        log.info(userStudentNum);
        if(myPageMapper.requestAccept(userStudentNum)){
            myPageMapper.requestAcceptData(userStudentNum);
            String referer = req.getHeader("Referer");
            return "redirect:"+ referer;
        }else return null;
    }
    // 마이페이지 > 멘티 상세보기
    /*@GetMapping("/whoIsMentee")
    public String whoIsMentee()*/



    //회원가입 페이지로 이동
    @GetMapping("/signUp")
    public String signUp(Model model, @ModelAttribute User user, @ModelAttribute Department department) {
        model.addAttribute("user", new User());
        model.addAttribute("departments" , mentoringBoardMapper.getDepartment());
        return  "signUp";
    }

    //회원가입 정보 등록
    @PostMapping("/signUp")
    public String singUp(Model model, @ModelAttribute User user){
        userMapper.addUser(user);
        return "login";
    }

    // 회원가입 id 중복확인
    @GetMapping("/checkId")
    @ResponseBody
    public String checkId(@RequestParam String id){
        log.info("들어옴");
        int result = userMapper.checkId(id);
        if (result == 0) {
            return "success";
        }
        else {
            return "fail";
        }
    }

    //회원가입 이메일 인증
    @GetMapping("/sendMail")
    @ResponseBody
    public String checkAuth(@RequestParam String email){
        log.info("메일 전송");
        String auth = mailService.sendMail(email);

        return auth;
    }


    @GetMapping("/deleteMentorBoard") // 멘토 게시글 삭제
    public String deleteMentorBoard(Long bno){
        mentoringBoardMapper.deleteMentorBoard(bno);
        return "redirect:/mentorStatus";
    }

    @GetMapping("/deleteMenteeBoard") // 멘티 게시글 삭제
    public String deleteMenteeBoard(Long bno){
        mentoringBoardMapper.deleteMenteeBoard(bno);
        return "redirect:/menteeStatus";
    }

}
