package com.jiangong.nmb.vo.statistics;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardTrendVO {
    private List<String> categories;
    private List<Integer> values;
} 