package com.cauh.iso.schedule;

import com.cauh.common.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

//@Component
@Slf4j
@RequiredArgsConstructor
public class UserRefreshScheduler {
    private final UserService userService;

//    @Scheduled(cron = "${scheduler.user-sync}")//초 분 시 일 월 요일 연(0시 1분)
//    public void userSync() {
//        userService.sync();
//    }
//
    //User 정보 갱신
    @Scheduled(cron = "${scheduler.user-refresh}")//초 분 시 일 월 요일 연(0시 01분)
    public void userRefresh() {
        userService.sync();
    }
}