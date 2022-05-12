package com.skhuMentoring.mapper;

import com.skhuMentoring.dto.Mentee;
import com.skhuMentoring.dto.Mentor;
import com.skhuMentoring.dto.Mentoring;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
@Mapper
@Repository
public interface MyPageMapper {
    //멘토현황
    public List<Mentor> getMentorStatus();
    //멘티현황
    public List<Mentee> getMenteeStatus();
    //내 멘티 현황
    public List<Mentee> getMenteeMyStatus(String userId);
    //내 멘토 현황
    public List<Mentor> getMentorMyStatus(String userId);
    //멘토링종료
    public void endMentoring(String subjectName);

    //멘토링수락
    public boolean requestAccept(Long bno);
    //멘토링거절
    public void requestRefusal(Long bno);

    //멘토링수락-데이터넣기
    public void requestAcceptData(Mentee mentee);

    public int whoIsMentee(Long bno);

    //멘토링 현황
    public List<Mentoring> getMentoring();
    //페이징 하면 사용
    /*public List<Mentor> getList(Criteria cri);
    public int getTotal(Criteria cri);*/

    //마이페이지 멘토에게 보낸 신청
    List<Mentee> getApplicationMentor(String menteeId);
    //마이페이지 멘티에게 받은 요청
    List<Mentee> getRequestMentee(String mentorId);
    //마이페이지 멘티에게 받은 요청에서 멘티의 상세보기
    Mentee getDetailMentee(Long bno);

}