package com.xyehyin.hexuanning.service;

import com.xyehyin.hexuanning.dto.paper.PaperCreateDTO;
import com.xyehyin.hexuanning.dto.paper.PaperUpdateDTO;
import com.xyehyin.hexuanning.dto.paper.PaperQuestionDTO;
import com.xyehyin.hexuanning.dto.paper.SmartPaperDTO;
import com.xyehyin.hexuanning.entity.Paper;
import com.xyehyin.hexuanning.entity.User;
import com.xyehyin.hexuanning.entity.PaperQuestion;
import com.xyehyin.hexuanning.entity.Question;
import com.xyehyin.hexuanning.mapper.PaperMapper;
import com.xyehyin.hexuanning.repository.*;
import com.xyehyin.hexuanning.mapper.QuestionMapper;
import com.xyehyin.hexuanning.vo.paper.PaperQuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.http.HttpStatus;
import com.xyehyin.hexuanning.vo.statistics.PaperStatisticsVO;
import com.xyehyin.hexuanning.entity.PracticeSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.xyehyin.hexuanning.constant.PermissionConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

/**
 * 试卷管理服务
 */
@Service
@RequiredArgsConstructor
public class PaperService {
    private final PaperRepository paperRepository;
    private final PaperMapper paperMapper;
    private final PaperQuestionRepository paperQuestionRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final PracticeSessionRepository practiceSessionRepository;
    private final UserRepository userRepository;

    /**
     * 创建试卷
     */
    @Transactional
    public Paper createPaper(PaperCreateDTO dto) {
        Paper paper = paperMapper.toPaper(dto);
        User teacher = userRepository.findById(dto.getTeacherId()).orElseThrow(() -> new StatefulException(HttpStatus.HTTP_NOT_FOUND, "教师不存在"));
        paper.setTeacher(teacher);
        paper.setStatus(Paper.PaperStatus.DRAFT);
        paper.setTotalScore(0);
        paper.setQuestions(new ArrayList<>());
        return paperRepository.save(paper);
    }

    /**
     * 分页查询试卷，支持按 title, teacherId, status 过滤
     */
    public Page<Paper> page(String title, Long teacherId, String status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page != null && page > 0 ? page - 1 : 0,
                size != null && size > 0 ? size : 10,
                Sort.by(Sort.Direction.DESC, "createTime"));
        return paperRepository.findAll((root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (title != null && !title.isEmpty()) {
                predicates.add(cb.like(root.get("title"), "%" + title + "%"));
            }
            if (teacherId != null) {
                predicates.add(cb.equal(root.get("teacher").get("id"), teacherId));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), Paper.PaperStatus.valueOf(status)));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);
    }

    /**
     * 根据ID查找试卷，不存在则抛异常
     */
    public Paper findByIdOrThrow(Long id) {
        return paperRepository.findById(id)
                .orElseThrow(() -> new StatefulException(HttpStatus.HTTP_NOT_FOUND, "试卷不存在"));
    }

    /**
     * 校验当前用户是否有权操作试卷（教师只能操作自己试卷，除非拥有对应的全局管理权限）
     */
    private void checkPaperOwnershipOrAdminPermission(Paper paper, Long currentUserId, String adminPermissionConstant) {
        if (paper.getTeacher() != null && paper.getTeacher().getId().equals(currentUserId)) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasAdminPermission = authentication != null && authentication.getAuthorities() != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> adminPermissionConstant.equals(grantedAuthority.getAuthority()));

        if (!hasAdminPermission) {
            throw new StatefulException(HttpStatus.HTTP_FORBIDDEN, "无权操作该试卷，非本人操作且无对应管理员权限");
        }
    }

    /**
     * 编辑试卷
     */
    @Transactional
    public Paper updatePaper(Long id, PaperUpdateDTO dto, Long currentUserId) {
        Paper paper = findByIdOrThrow(id);
        checkPaperOwnershipOrAdminPermission(paper, currentUserId, PermissionConstants.PAPER_EDIT_ALL);
        paperMapper.updatePaperFromDto(dto, paper);
        return paperRepository.save(paper);
    }

    /**
     * 删除试卷
     */
    @Transactional
    public void deletePaper(Long id, Long currentUserId) {
        Paper paper = findByIdOrThrow(id);
        checkPaperOwnershipOrAdminPermission(paper, currentUserId, PermissionConstants.PAPER_DELETE_ALL);
        paperRepository.delete(paper);
    }

    /**
     * 发布试卷，发布前做业务校验
     */
    @Transactional
    public void publishPaper(Long id, Long currentUserId) {
        Paper paper = findByIdOrThrow(id);
        checkPaperOwnershipOrAdminPermission(paper, currentUserId, PermissionConstants.PAPER_PUBLISH_ALL);
        // 校验：不能重复发布
        if (paper.getStatus() == Paper.PaperStatus.PUBLISHED) {
            throw new cn.hutool.core.exceptions.StatefulException(cn.hutool.http.HttpStatus.HTTP_BAD_REQUEST, "试卷已发布");
        }
        // 校验：题目数量
        int questionCount = paperQuestionRepository.findByPaperId(id).size();
        if (questionCount == 0) {
            throw new cn.hutool.core.exceptions.StatefulException(cn.hutool.http.HttpStatus.HTTP_BAD_REQUEST, "试卷没有题目，不能发布");
        }
        // 校验：总分
        if (paper.getTotalScore() == null || paper.getTotalScore() <= 0) {
            throw new cn.hutool.core.exceptions.StatefulException(cn.hutool.http.HttpStatus.HTTP_BAD_REQUEST, "试卷总分无效，不能发布");
        }
        paper.setStatus(Paper.PaperStatus.PUBLISHED);
        paperRepository.save(paper);
    }

    /**
     * 归档试卷，归档前做业务校验
     */
    @Transactional
    public void archivePaper(Long id, Long currentUserId) {
        Paper paper = findByIdOrThrow(id);
        checkPaperOwnershipOrAdminPermission(paper, currentUserId, PermissionConstants.PAPER_ARCHIVE_ALL);
        // 校验：不能归档未发布试卷
        if (paper.getStatus() != Paper.PaperStatus.PUBLISHED) {
            throw new cn.hutool.core.exceptions.StatefulException(cn.hutool.http.HttpStatus.HTTP_BAD_REQUEST, "未发布试卷不能归档");
        }
        paper.setStatus(Paper.PaperStatus.ARCHIVED);
        paperRepository.save(paper);
    }

    /**
     * 批量添加题目到试卷，幂等校验
     */
    @Transactional
    public void addQuestions(Long paperId, List<PaperQuestionDTO> dtos, Long currentUserId) {
        Paper paper = findByIdOrThrow(paperId);
        checkPaperOwnershipOrAdminPermission(paper, currentUserId, PermissionConstants.PAPER_EDIT_ALL);
        for (PaperQuestionDTO dto : dtos) {
            if (paperQuestionRepository.findByPaperIdAndQuestionId(paperId, dto.getQuestionId()).isPresent()) {
                continue;
            }
            PaperQuestion pq = new PaperQuestion();
            pq.setPaper(paper);
            pq.setQuestion(new com.xyehyin.hexuanning.entity.Question());
            pq.getQuestion().setId(dto.getQuestionId());
            pq.setOrderNum(dto.getOrderNum() != null ? dto.getOrderNum() : 0);
            pq.setScore(dto.getScore() != null ? dto.getScore() : 1);
            paperQuestionRepository.save(pq);
        }
        // 重新计算总分
        List<PaperQuestion> questions = paperQuestionRepository.findByPaperId(paperId);
        int totalScore = questions.stream().mapToInt(PaperQuestion::getScore).sum();
        paper.setTotalScore(totalScore);
        paperRepository.save(paper);
    }

    /**
     * 移除试卷中的题目
     */
    @Transactional
    public void removeQuestion(Long paperId, Long questionId, Long currentUserId) {
        Paper paper = findByIdOrThrow(paperId);
        checkPaperOwnershipOrAdminPermission(paper, currentUserId, PermissionConstants.PAPER_EDIT_ALL);
        paperQuestionRepository.findByPaperIdAndQuestionId(paperId, questionId)
                .ifPresent(paperQuestionRepository::delete);
        // 重新计算总分
        List<PaperQuestion> questions = paperQuestionRepository.findByPaperId(paperId);
        int totalScore = questions.stream().mapToInt(PaperQuestion::getScore).sum();
        paper.setTotalScore(totalScore);
        paperRepository.save(paper);
    }

    /**
     * 批量调整题目顺序和分值
     */
    @Transactional
    public void updateQuestions(Long paperId, List<PaperQuestionDTO> dtos, Long currentUserId) {
        Paper paper = findByIdOrThrow(paperId);
        checkPaperOwnershipOrAdminPermission(paper, currentUserId, PermissionConstants.PAPER_EDIT_ALL);
        for (PaperQuestionDTO dto : dtos) {
            paperQuestionRepository.findByPaperIdAndQuestionId(paperId, dto.getQuestionId())
                    .ifPresent(pq -> {
                        if (dto.getOrderNum() != null) pq.setOrderNum(dto.getOrderNum());
                        if (dto.getScore() != null) pq.setScore(dto.getScore());
                        paperQuestionRepository.save(pq);
                    });
        }
    }

    /**
     * 查询试卷题目列表，支持乱序
     */
    public List<PaperQuestionVO> listQuestions(Long paperId, boolean randomOrder) {
        Paper paper = findByIdOrThrow(paperId);
        List<PaperQuestion> list = paperQuestionRepository.findByPaperId(paperId);
        if (randomOrder) {
            java.util.Collections.shuffle(list);
        } else {
            list = list.stream().sorted(java.util.Comparator.comparing(PaperQuestion::getOrderNum)).toList();
        }
        List<Long> qids = list.stream()
                .map(pq -> pq.getQuestion() != null ? pq.getQuestion().getId() : null)
                .filter(java.util.Objects::nonNull)
                .toList();
        List<Question> questions = questionRepository.findAllById(qids);
        java.util.Map<Long, Question> qmap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));
        List<PaperQuestionVO> voList = new ArrayList<>();
        for (PaperQuestion pq : list) {
            if (pq.getQuestion() == null || pq.getQuestion().getId() == null) continue;
            PaperQuestionVO vo = new PaperQuestionVO();
            vo.setQuestionId(pq.getQuestion().getId());
            vo.setOrderNum(pq.getOrderNum());
            vo.setScore(pq.getScore());
            Question q = qmap.get(pq.getQuestion().getId());
            if (q != null) {
                vo.setTitle(q.getTitle());
                vo.setType(q.getType() != null ? q.getType().name() : null);
            }
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 智能组卷，根据条件自动筛选题目并生成试卷
     */
    @Transactional
    public Paper smartGenerate(SmartPaperDTO dto, Long teacherId) {
        // 1. 构建筛选条件
        List<Question> all = new ArrayList<>();
        if (dto.getKnowledgePointIds() != null && !dto.getKnowledgePointIds().isEmpty()) {
            for (Long kpId : dto.getKnowledgePointIds()) {
                List<Question> qs = questionRepository.findByKnowledgePointIdAndEnabled(kpId, true);
                all.addAll(qs);
            }
        } else {
            List<Question> foundQuestions = questionRepository.findAll();
            all = (foundQuestions == null) ? new ArrayList<>() : new ArrayList<>(foundQuestions);
        }
        // 2. 按难度、题型过滤
        if (dto.getDifficulty() != null) {
            all = all.stream()
                    .filter(q -> q.getDifficulty() != null && q.getDifficulty().name().equalsIgnoreCase(dto.getDifficulty()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        if (dto.getQuestionTypes() != null && !dto.getQuestionTypes().isEmpty()) {
            all = all.stream()
                    .filter(q -> q.getType() != null && dto.getQuestionTypes().contains(q.getType().name()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        // 3. 按题型分布优先选题
        List<Question> selected = new ArrayList<>();
        if (dto.getQuestionTypeDistribution() != null && !dto.getQuestionTypeDistribution().isEmpty()) {
            Map<String, Integer> dist = dto.getQuestionTypeDistribution();
            for (Map.Entry<String, Integer> entry : dist.entrySet()) {
                String type = entry.getKey();
                int num = entry.getValue();
                List<Question> pool = all.stream()
                        .filter(q -> q.getType() != null && q.getType().name().equals(type))
                        .collect(Collectors.toCollection(ArrayList::new));
                java.util.Collections.shuffle(pool);
                selected.addAll(pool.stream().limit(num).collect(Collectors.toList()));
            }
        }
        // 4. 剩余题目随机补齐
        int total = dto.getTotalQuestions() != null ? dto.getTotalQuestions() : 10;
        if (selected.size() < total) {
            List<Question> remain = new ArrayList<>(all);
            remain.removeAll(selected);
            java.util.Collections.shuffle(remain);
            selected.addAll(remain.stream().limit(total - selected.size()).collect(Collectors.toList()));
        }
        if (selected.size() > total) {
            selected = new ArrayList<>(selected.subList(0, total));
        }
        // 5. 创建试卷
        Paper paper = new Paper();
        paper.setTitle("智能组卷-" + System.currentTimeMillis());
        paper.setDescription("本试卷由系统智能组卷自动生成。");
        paper.setStatus(Paper.PaperStatus.DRAFT);
        User teacher = userRepository.findById(teacherId).orElseThrow(() -> new StatefulException(HttpStatus.HTTP_NOT_FOUND, "教师不存在"));
        paper.setTeacher(teacher);
        // 设置时间限制
        if (dto.getTimeLimit() != null) {
            paper.setTimeLimit(dto.getTimeLimit());
        }
        paper.setQuestions(new ArrayList<>());
        paper = paperRepository.save(paper);
        // 6. 关联题目
        int order = 1;
        for (Question q : selected) {
            if (q.getId() == null) continue; // 跳过无效题目
            PaperQuestion pq = new PaperQuestion();
            pq.setPaper(paper);
            pq.setQuestion(questionRepository.getReferenceById(q.getId()));
            pq.setOrderNum(order++);
            pq.setScore(q.getScore() != null ? q.getScore() : 1);
            paperQuestionRepository.save(pq);
        }
        // 7. 计算总分
        int totalScore = selected.stream().mapToInt(q -> q.getScore() != null ? q.getScore() : 1).sum();
        paper.setTotalScore(totalScore);
        return paperRepository.save(paper);
    }

    /**
     * 统计试卷完成情况、分数、正确率等
     */
    public PaperStatisticsVO statistics(Long paperId) {
        Paper paper = findByIdOrThrow(paperId);
        List<PracticeSession> sessions = practiceSessionRepository.findAll().stream()
                .filter(s -> s.getPaper().getId().equals(paperId) && Boolean.TRUE.equals(s.getSubmitted()))
                .toList();
        PaperStatisticsVO vo = new PaperStatisticsVO();
        vo.setPaperId(paperId);
        vo.setPaperTitle(paper.getTitle());
        vo.setTotalAttempts(sessions.size());
        if (!sessions.isEmpty()) {
            double avg = sessions.stream().mapToDouble(s -> s.getTotalScore() != null ? s.getTotalScore() : 0).average().orElse(0);
            double max = sessions.stream().mapToDouble(s -> s.getTotalScore() != null ? s.getTotalScore() : 0).max().orElse(0);
            double min = sessions.stream().mapToDouble(s -> s.getTotalScore() != null ? s.getTotalScore() : 0).min().orElse(0);
            double correctRate = sessions.stream().mapToDouble(s -> s.getCorrectRate() != null ? s.getCorrectRate() : 0).average().orElse(0);
            vo.setAverageScore(avg);
            vo.setHighestScore(max);
            vo.setLowestScore(min);
            vo.setCorrectRate(correctRate);
            // 及格率
            int passCount = (int) sessions.stream().filter(s -> s.getTotalScore() != null && s.getTotalScore() >= (paper.getTotalScore() * 0.6)).count();
            vo.setPassRate(sessions.size() > 0 ? (double) passCount / sessions.size() : 0.0);
            // 成绩分布
            Map<String, Integer> scoreDist = new HashMap<>();
            int[] ranges = {0, 60, 70, 80, 90, 100};
            String[] labels = {"0-59", "60-69", "70-79", "80-89", "90-100"};
            for (String label : labels) scoreDist.put(label, 0);
            for (PracticeSession s : sessions) {
                double score = s.getTotalScore() != null ? s.getTotalScore() : 0;
                double percent = paper.getTotalScore() != null && paper.getTotalScore() > 0 ? (score / paper.getTotalScore()) * 100 : 0;
                if (percent < 60) scoreDist.put("0-59", scoreDist.get("0-59") + 1);
                else if (percent < 70) scoreDist.put("60-69", scoreDist.get("60-69") + 1);
                else if (percent < 80) scoreDist.put("70-79", scoreDist.get("70-79") + 1);
                else if (percent < 90) scoreDist.put("80-89", scoreDist.get("80-89") + 1);
                else scoreDist.put("90-100", scoreDist.get("90-100") + 1);
            }
            vo.setScoreDistribution(scoreDist);
            // 每题正确率、易错题
            List<PaperQuestion> paperQuestions = paperQuestionRepository.findByPaperId(paperId);
            List<PaperStatisticsVO.QuestionCorrectRate> qcrList = new ArrayList<>();
            List<PaperStatisticsVO.WrongQuestion> wqList = new ArrayList<>();
            for (PaperQuestion pq : paperQuestions) {
                Long qid = pq.getQuestion().getId();
                String qtitle = pq.getQuestion().getTitle();
                int total = 0, correct = 0, wrong = 0;
                for (PracticeSession s : sessions) {
                    List<com.xyehyin.hexuanning.entity.StudentAnswer> answers = s.getAnswers();
                    if (answers != null) {
                        for (com.xyehyin.hexuanning.entity.StudentAnswer a : answers) {
                            if (a.getQuestion().getId().equals(qid)) {
                                total++;
                                if (Boolean.TRUE.equals(a.getCorrect())) correct++;
                                else wrong++;
                            }
                        }
                    }
                }
                PaperStatisticsVO.QuestionCorrectRate qcr = new PaperStatisticsVO.QuestionCorrectRate();
                qcr.setQuestionId(qid);
                qcr.setQuestionTitle(qtitle);
                qcr.setCorrectRate(total > 0 ? (double) correct / total : 0.0);
                qcrList.add(qcr);
                if (wrong > 0) {
                    PaperStatisticsVO.WrongQuestion wq = new PaperStatisticsVO.WrongQuestion();
                    wq.setQuestionId(qid);
                    wq.setQuestionTitle(qtitle);
                    wq.setWrongCount(wrong);
                    wqList.add(wq);
                }
            }
            vo.setQuestionCorrectRates(qcrList);
            vo.setWrongQuestions(wqList);
        } else {
            vo.setAverageScore(0.0);
            vo.setHighestScore(0.0);
            vo.setLowestScore(0.0);
            vo.setCorrectRate(0.0);
            vo.setPassRate(0.0);
            vo.setScoreDistribution(new HashMap<>());
            vo.setQuestionCorrectRates(new ArrayList<>());
            vo.setWrongQuestions(new ArrayList<>());
        }
        return vo;
    }
}
