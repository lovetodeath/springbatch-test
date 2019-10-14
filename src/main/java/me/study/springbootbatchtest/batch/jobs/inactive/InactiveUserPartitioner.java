package me.study.springbootbatchtest.batch.jobs.inactive;

import me.study.springbootbatchtest.batch.domain.enums.Grade;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class InactiveUserPartitioner implements Partitioner {
    private static final String GRADE = "grade";
    private static final String INACTIVE_USER_TASK = "InaciveUserTask";

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        // gridSize만큼 Map 크기 할당
        Map<String, ExecutionContext> map = new HashMap<>(gridSize);
        // Grade Enum에 정의된 모든 값을 grades 배열 변수로 할당
        Grade[] grades = Grade.values();
        // grades 값만큼 loop 수행하여, 파티션 생성
        for(int i = 0, length = grades.length; i < length; i++) {
            ExecutionContext executionContext = new ExecutionContext();
            // 'grade' 키이름으로 Grade ENUM의 이름값 삽입
            executionContext.putString(GRADE, grades[i].name());
            // InactiveUserTask1~x 키이름으로 ExecutionContext 삽입
            map.put(INACTIVE_USER_TASK +i, executionContext);
        }
        return map;
    }
}
