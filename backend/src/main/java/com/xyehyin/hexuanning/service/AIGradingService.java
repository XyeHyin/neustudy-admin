package com.xyehyin.hexuanning.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import cn.hutool.json.JSONArray;

/**
 * AI判分服务
 */
@Service
@RequiredArgsConstructor
public class AIGradingService {
    private final ChatLanguageModel chatLanguageModel;

    /**
     * 调用大模型进行AI判分
     *
     * @param questionContent 题目内容
     * @param studentAnswer   学生答案
     * @param maxScore        题目满分
     * @return AI判分结果（分数+评语）
     */
    public AIGradingResult gradeAnswer(String questionContent, String studentAnswer, double maxScore) {
        // 构建 prompt，明确告知满分及返回 reason
        String prompt = "请对以下学生答案进行判分，并给出简短评语和详细判分理由。\n题目：" + questionContent + "\n学生答案：" + studentAnswer + "\n本题满分为" + maxScore + "分，请以如下JSON格式返回：{\"score\":分数,\"comment\":\"简短评语\",\"reason\":\"详细判分理由\"}，分数不得超过满分。";
        String aiResponse = chatLanguageModel.generate(prompt);
        double score = 0.0;
        String comment = "";
        String reason = "";
        try {
            JSONObject json = JSONUtil.parseObj(aiResponse);
            score = json.getDouble("score", 0.0);
            // 分数限定在 0~maxScore
            if (score < 0) score = 0.0;
            if (score > maxScore) score = maxScore;
            comment = json.getStr("comment", "");
            reason = json.getStr("reason", "");
        } catch (Exception e) {
            comment = "AI判分解析失败，原始返回：" + aiResponse;
            reason = comment;
        }
        return new AIGradingResult(score, comment, reason);
    }

    public record AIGradingResult(double score, String comment, String reason) {
    }

    /**
     * 批量判分请求对象
     */
    public record AIRequest(String questionContent, String studentAnswer, double maxScore) {
    }

    /**
     * 批量调用大模型进行AI判分
     * @param requests 待判分的题目列表
     * @return AI判分结果列表，顺序与请求列表一致
     */
    public List<AIGradingResult> batchGradeAnswers(List<AIRequest> requests) {
        // 构建批量 prompt
        StringBuilder prompt = new StringBuilder()
                .append("请对以下多道题目和学生答案进行批量判分并给出简短评语，返回纯 JSON 数组，格式示例：");
        prompt.append("[");
        for (int i = 0; i < requests.size(); i++) {
            AIRequest r = requests.get(i);
            prompt.append("{\"question\":\"")
                  .append(r.questionContent())
                  .append("\",\"answer\":\"")
                  .append(r.studentAnswer())
                  .append("\",\"maxScore\":")
                  .append(r.maxScore())
                  .append("}");
            if (i < requests.size() - 1) prompt.append(",");
        }
        prompt.append("]");
        String aiResponse = chatLanguageModel.generate(prompt.toString());
        List<AIGradingResult> results = new ArrayList<>();
        try {
            JSONArray arr = JSONUtil.parseArray(aiResponse);
            for (int i = 0; i < arr.size(); i++) {
                JSONObject json = arr.getJSONObject(i);
                double score = json.getDouble("score", 0.0);
                if (score < 0) score = 0.0;
                double max = requests.get(i).maxScore();
                if (score > max) score = max;
                String comment = json.getStr("comment", "");
                String reason = json.getStr("reason", "");
                results.add(new AIGradingResult(score, comment, reason));
            }
        } catch (Exception e) {
            // 解析失败时统一返回错误信息
            for (int i = 0; i < requests.size(); i++) {
                results.add(new AIGradingResult(0.0, "", "AI判分解析失败，原始返回：" + aiResponse));
            }
        }
        return results;
    }
} 