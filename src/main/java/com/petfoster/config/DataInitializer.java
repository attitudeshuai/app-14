package com.petfoster.config;

import com.petfoster.entity.*;
import com.petfoster.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final FosterRequestRepository fosterRequestRepository;
    private final FosterDailyLogRepository dailyLogRepository;
    private final FosterReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("检测到已有数据，跳过数据初始化");
            return;
        }

        log.info("开始初始化示例数据...");

        createUsers();
        createPets();
        createFosterRequests();
        createDailyLogs();
        createReviews();

        log.info("示例数据初始化完成！");
    }

    private void createUsers() {
        User user1 = User.builder()
                .username("zhangsan")
                .email("zhangsan@example.com")
                .passwordHash(passwordEncoder.encode("123456"))
                .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan")
                .role(User.Role.ADMIN)
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .username("lisi")
                .email("lisi@example.com")
                .passwordHash(passwordEncoder.encode("123456"))
                .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=lisi")
                .build();
        userRepository.save(user2);

        User user3 = User.builder()
                .username("wangwu")
                .email("wangwu@example.com")
                .passwordHash(passwordEncoder.encode("123456"))
                .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu")
                .build();
        userRepository.save(user3);

        User user4 = User.builder()
                .username("zhaoliu")
                .email("zhaoliu@example.com")
                .passwordHash(passwordEncoder.encode("123456"))
                .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=zhaoliu")
                .build();
        userRepository.save(user4);

        User user5 = User.builder()
                .username("sunqi")
                .email("sunqi@example.com")
                .passwordHash(passwordEncoder.encode("123456"))
                .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=sunqi")
                .build();
        userRepository.save(user5);

        log.info("已创建5个示例用户");
    }

    private void createPets() {
        Pet pet1 = Pet.builder()
                .ownerId(1L)
                .name("小白")
                .species("猫")
                .breed("英国短毛猫")
                .age(3)
                .dietNotes("每天3次，每次30克猫粮，早晚各1次湿粮")
                .medicalNotes("已绝育，定期驱虫，无过敏史")
                .photoUrl("https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400")
                .build();
        petRepository.save(pet1);

        Pet pet2 = Pet.builder()
                .ownerId(1L)
                .name("大黄")
                .species("狗")
                .breed("金毛寻回犬")
                .age(5)
                .dietNotes("每天2次，每次200克狗粮，需要大量运动")
                .medicalNotes("对鸡肉过敏，已打狂犬疫苗")
                .photoUrl("https://images.unsplash.com/photo-1552053831-71594a27632d?w=400")
                .build();
        petRepository.save(pet2);

        Pet pet3 = Pet.builder()
                .ownerId(2L)
                .name("咪咪")
                .species("猫")
                .breed("布偶猫")
                .age(2)
                .dietNotes("自由采食猫粮，每天加水煮鸡胸肉")
                .medicalNotes("胆小，容易应激，需安静环境")
                .photoUrl("https://images.unsplash.com/photo-1592194996308-7b43878e84a6?w=400")
                .build();
        petRepository.save(pet3);

        Pet pet4 = Pet.builder()
                .ownerId(3L)
                .name("旺财")
                .species("狗")
                .breed("柴犬")
                .age(4)
                .dietNotes("每天2次，每次150克，零食适量")
                .medicalNotes("性格活泼，需要每天遛狗2次")
                .photoUrl("https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=400")
                .build();
        petRepository.save(pet4);

        Pet pet5 = Pet.builder()
                .ownerId(4L)
                .name("球球")
                .species("其他")
                .breed("荷兰猪")
                .age(1)
                .dietNotes("干草无限量，每天少量蔬果")
                .medicalNotes("胆小，不要吓到它")
                .photoUrl("https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=400")
                .build();
        petRepository.save(pet5);

        Pet pet6 = Pet.builder()
                .ownerId(5L)
                .name("豆豆")
                .species("猫")
                .breed("美短")
                .age(6)
                .dietNotes("老年猫粮，少食多餐")
                .medicalNotes("肾功能不太好，需定期复查")
                .photoUrl("https://images.unsplash.com/photo-1574158622682-e40e69881006?w=400")
                .build();
        petRepository.save(pet6);

        Pet pet7 = Pet.builder()
                .ownerId(2L)
                .name("小黑")
                .species("狗")
                .breed("拉布拉多")
                .age(2)
                .dietNotes("每天3次，运动量大")
                .medicalNotes("正在训练中，很聪明")
                .photoUrl("https://images.unsplash.com/photo-1583337130417-3346a1be7dee?w=400")
                .build();
        petRepository.save(pet7);

        Pet pet8 = Pet.builder()
                .ownerId(3L)
                .name("花花")
                .species("猫")
                .breed("三花猫")
                .age(4)
                .dietNotes("正常猫粮即可")
                .medicalNotes("身体健康，性格温顺")
                .photoUrl("https://images.unsplash.com/photo-1561948955-570b270e7c36?w=400")
                .build();
        petRepository.save(pet8);

        log.info("已创建8个示例宠物");
    }

    private void createFosterRequests() {
        FosterRequest req1 = FosterRequest.builder()
                .petId(1L)
                .ownerId(1L)
                .fostererId(2L)
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().minusDays(5))
                .dailyCareNotes("每天早晚各喂一次，记得清理猫砂盆")
                .status(FosterRequest.Status.Completed)
                .build();
        fosterRequestRepository.save(req1);

        FosterRequest req2 = FosterRequest.builder()
                .petId(3L)
                .ownerId(2L)
                .fostererId(1L)
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now().plusDays(3))
                .dailyCareNotes("咪咪比较胆小，尽量不要打扰它，定时喂食换水")
                .status(FosterRequest.Status.InProgress)
                .build();
        fosterRequestRepository.save(req2);

        FosterRequest req3 = FosterRequest.builder()
                .petId(2L)
                .ownerId(1L)
                .fostererId(3L)
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(12))
                .dailyCareNotes("大黄需要每天遛狗至少1小时，运动量比较大")
                .status(FosterRequest.Status.Approved)
                .build();
        fosterRequestRepository.save(req3);

        FosterRequest req4 = FosterRequest.builder()
                .petId(4L)
                .ownerId(3L)
                .fostererId(null)
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(17))
                .dailyCareNotes("旺财活泼好动，需要有耐心的邻居帮忙照顾")
                .status(FosterRequest.Status.Pending)
                .build();
        fosterRequestRepository.save(req4);

        FosterRequest req5 = FosterRequest.builder()
                .petId(5L)
                .ownerId(4L)
                .fostererId(5L)
                .startDate(LocalDate.now().minusDays(3))
                .endDate(LocalDate.now().plusDays(5))
                .dailyCareNotes("球球主要吃干草，记得每天换新鲜的水")
                .status(FosterRequest.Status.InProgress)
                .build();
        fosterRequestRepository.save(req5);

        FosterRequest req6 = FosterRequest.builder()
                .petId(6L)
                .ownerId(5L)
                .fostererId(null)
                .startDate(LocalDate.now().plusDays(15))
                .endDate(LocalDate.now().plusDays(25))
                .dailyCareNotes("豆豆是老年猫，需要特殊照顾，定时喂食肾脏处方粮")
                .status(FosterRequest.Status.Pending)
                .build();
        fosterRequestRepository.save(req6);

        FosterRequest req7 = FosterRequest.builder()
                .petId(7L)
                .ownerId(2L)
                .fostererId(4L)
                .startDate(LocalDate.now().minusDays(20))
                .endDate(LocalDate.now().minusDays(15))
                .dailyCareNotes("小黑训练期，每天多互动训练")
                .status(FosterRequest.Status.Completed)
                .build();
        fosterRequestRepository.save(req7);

        FosterRequest req8 = FosterRequest.builder()
                .petId(8L)
                .ownerId(3L)
                .fostererId(null)
                .startDate(LocalDate.now().plusDays(3))
                .endDate(LocalDate.now().plusDays(8))
                .dailyCareNotes("花花很独立，只要定时喂食就行")
                .status(FosterRequest.Status.Cancelled)
                .build();
        fosterRequestRepository.save(req8);

        log.info("已创建8个示例寄养申请");
    }

    private void createDailyLogs() {
        FosterDailyLog log1 = FosterDailyLog.builder()
                .requestId(1L)
                .fostererId(2L)
                .logDate(LocalDate.now().minusDays(10))
                .food("早餐30g猫粮，晚餐30g猫粮，晚上加了一点湿粮")
                .mood("第一天有点紧张，躲在沙发下面，晚上出来吃东西了")
                .photos("https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=300")
                .note("整体情况还可以，慢慢适应中")
                .build();
        dailyLogRepository.save(log1);

        FosterDailyLog log2 = FosterDailyLog.builder()
                .requestId(1L)
                .fostererId(2L)
                .logDate(LocalDate.now().minusDays(9))
                .food("正常喂食，水也喝了不少")
                .mood("今天好多了，愿意出来走动了")
                .photos("https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=301")
                .note("猫砂盆清理正常")
                .build();
        dailyLogRepository.save(log2);

        FosterDailyLog log3 = FosterDailyLog.builder()
                .requestId(1L)
                .fostererId(2L)
                .logDate(LocalDate.now().minusDays(8))
                .food("食欲很好")
                .mood("非常活泼，开始玩逗猫棒了")
                .photos("https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=302")
                .note("适应得很好")
                .build();
        dailyLogRepository.save(log3);

        FosterDailyLog log4 = FosterDailyLog.builder()
                .requestId(2L)
                .fostererId(1L)
                .logDate(LocalDate.now().minusDays(7))
                .food("正常喂食")
                .mood("很安静，一直躲在床底下")
                .photos("https://images.unsplash.com/photo-1592194996308-7b43878e84a6?w=300")
                .note("希望明天能好一些")
                .build();
        dailyLogRepository.save(log4);

        FosterDailyLog log5 = FosterDailyLog.builder()
                .requestId(2L)
                .fostererId(1L)
                .logDate(LocalDate.now().minusDays(6))
                .food("只吃了一点点湿粮")
                .mood("还是很紧张")
                .photos("https://images.unsplash.com/photo-1592194996308-7b43878e84a6?w=301")
                .note("放了一些它熟悉的毯子")
                .build();
        dailyLogRepository.save(log5);

        FosterDailyLog log6 = FosterDailyLog.builder()
                .requestId(2L)
                .fostererId(1L)
                .logDate(LocalDate.now().minusDays(5))
                .food("食欲恢复正常了")
                .mood("终于出来溜达了")
                .photos("https://images.unsplash.com/photo-1592194996308-7b43878e84a6?w=302")
                .note("状态越来越好")
                .build();
        dailyLogRepository.save(log6);

        FosterDailyLog log7 = FosterDailyLog.builder()
                .requestId(5L)
                .fostererId(5L)
                .logDate(LocalDate.now().minusDays(3))
                .food("干草和少量胡萝卜")
                .mood("很活泼")
                .photos("https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=300")
                .note("状态很好")
                .build();
        dailyLogRepository.save(log7);

        FosterDailyLog log8 = FosterDailyLog.builder()
                .requestId(5L)
                .fostererId(5L)
                .logDate(LocalDate.now().minusDays(2))
                .food("正常饮食")
                .mood("很开心地吃东西")
                .photos("https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=301")
                .note("一切正常")
                .build();
        dailyLogRepository.save(log8);

        log.info("已创建8条示例寄养日报");
    }

    private void createReviews() {
        FosterReview review1 = FosterReview.builder()
                .requestId(1L)
                .reviewerId(1L)
                .revieweeId(2L)
                .rating(5)
                .content("非常感谢李四的照顾！小白回来状态很好，每天都有照片和详细的日报，太让人放心了！强烈推荐！")
                .build();
        reviewRepository.save(review1);

        FosterReview review2 = FosterReview.builder()
                .requestId(1L)
                .reviewerId(2L)
                .revieweeId(1L)
                .rating(5)
                .content("小白真的太可爱了！很乖很听话，照顾它是件愉快的事。张三主人交代得很清楚，沟通顺畅。")
                .build();
        reviewRepository.save(review2);

        FosterReview review3 = FosterReview.builder()
                .requestId(7L)
                .reviewerId(2L)
                .revieweeId(4L)
                .rating(4)
                .content("整体不错，小黑照顾得挺好的。如果遛狗时间能再长一点就更完美了。")
                .build();
        reviewRepository.save(review3);

        FosterReview review4 = FosterReview.builder()
                .requestId(7L)
                .reviewerId(4L)
                .revieweeId(2L)
                .rating(5)
                .content("小黑非常听话，训练有素！二黑主人也非常nice，沟通很愉快。期待下次合作！")
                .build();
        reviewRepository.save(review4);

        log.info("已创建4条示例评价");
    }
}
