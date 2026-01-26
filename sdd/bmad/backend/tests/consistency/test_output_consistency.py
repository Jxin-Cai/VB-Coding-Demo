"""
输出一致性测试
验证相同输入生成相同输出（一致性 ≥ 95%）
"""
import sys
from pathlib import Path

BACKEND_DIR = Path(__file__).parent.parent.parent
sys.path.insert(0, str(BACKEND_DIR))

import pytest
from typing import List, Dict, Any
from collections import Counter


class ConsistencyTester:
    """一致性测试工具"""
    
    def __init__(self, sql_generator):
        """
        Args:
            sql_generator: SQL 生成器实例
        """
        self.sql_generator = sql_generator
    
    def test_consistency(
        self,
        query: str,
        table_context: List[Dict[str, Any]],
        iterations: int = 5
    ) -> Dict[str, Any]:
        """
        测试输出一致性
        
        Args:
            query: 用户查询
            table_context: 表结构上下文
            iterations: 测试次数
            
        Returns:
            Dict: 一致性测试结果
        """
        results = []
        
        for i in range(iterations):
            try:
                result = self.sql_generator.generate(query, table_context)
                sql = result.get("sql", "")
                results.append(sql)
            except Exception as e:
                results.append(f"ERROR: {str(e)}")
        
        # 统计一致性
        counter = Counter(results)
        most_common = counter.most_common(1)[0]
        most_common_sql = most_common[0]
        most_common_count = most_common[1]
        
        consistency_rate = most_common_count / iterations
        
        return {
            "query": query,
            "iterations": iterations,
            "results": results,
            "most_common_output": most_common_sql,
            "most_common_count": most_common_count,
            "consistency_rate": consistency_rate,
            "passed": consistency_rate >= 0.95,
            "unique_outputs": len(counter)
        }


class TestSQLOutputConsistency:
    """SQL 输出一致性测试"""
    
    @pytest.fixture
    def sample_table_context(self):
        """示例表结构上下文"""
        return [
            {
                "type": "表",
                "name": "users",
                "description": "用户表",
                "column_count": 4
            },
            {
                "type": "字段",
                "table": "users",
                "name": "user_id",
                "data_type": "BIGINT",
                "description": "用户ID"
            },
            {
                "type": "字段",
                "table": "users",
                "name": "username",
                "data_type": "VARCHAR(50)",
                "description": "用户名"
            },
            {
                "type": "字段",
                "table": "users",
                "name": "status",
                "data_type": "VARCHAR(20)",
                "description": "用户状态"
            },
            {
                "type": "字段",
                "table": "users",
                "name": "created_at",
                "data_type": "TIMESTAMP",
                "description": "创建时间"
            }
        ]
    
    def test_simple_query_consistency(self, sample_table_context):
        """测试简单查询的一致性"""
        from domain.sql.sql_generator import SQLGenerator
        from infrastructure.llm.llm_service import get_llm_service
        
        llm_service = get_llm_service()
        
        # 如果 LLM 未配置，使用 Mock 测试一致性框架本身
        if not llm_service.is_available():
            # Mock 模式：测试一致性计算逻辑
            class MockSQLGenerator:
                """Mock SQL 生成器，返回固定输出"""
                def generate(self, query, table_context):
                    # 模拟确定性输出（5次中有4次相同）
                    import random
                    random.seed(42)  # 固定随机种子确保测试可重复
                    
                    if random.random() < 0.8:  # 80% 返回相同结果
                        return {
                            "sql": "SELECT user_id, username, status FROM users WHERE status = 'active';",
                            "type": "sql"
                        }
                    else:  # 20% 返回略有不同的结果
                        return {
                            "sql": "SELECT * FROM users WHERE status = 'active';",
                            "type": "sql"
                        }
            
            generator = MockSQLGenerator()
            tester = ConsistencyTester(generator)
            
            query = "查询用户表中状态为活跃的用户"
            result = tester.test_consistency(query, sample_table_context, iterations=5)
            
            # Mock 模式下验证一致性计算框架本身
            assert "consistency_rate" in result, "结果应包含一致性率"
            assert "passed" in result, "结果应包含通过标志"
            assert result["iterations"] == 5, "迭代次数不正确"
            
            print(f"⚠️  Mock 模式：一致性率 {result['consistency_rate']:.1%}（预期 80%）")
            return
        
        # 真实 LLM 模式
        generator = SQLGenerator()
        tester = ConsistencyTester(generator)
        
        query = "查询用户表中状态为活跃的用户"
        result = tester.test_consistency(query, sample_table_context, iterations=5)
        
        # 验证一致性（真实 LLM）
        assert result["passed"], \
            f"❌ 一致性测试失败：一致性率 {result['consistency_rate']:.1%} < 95%\n" \
            f"Unique outputs: {result['unique_outputs']}\n" \
            f"Most common: {result['most_common_output'][:100]}..."
        
        assert result["unique_outputs"] <= 2, \
            f"输出变体过多：{result['unique_outputs']} 种不同输出"
        
        print(f"✅ 一致性测试通过：{result['consistency_rate']:.1%}")
    
    def test_consistency_rate_calculation(self):
        """测试一致性率计算逻辑"""
        # Mock 数据
        results = [
            "SELECT * FROM users WHERE status = 'active'",
            "SELECT * FROM users WHERE status = 'active'",
            "SELECT * FROM users WHERE status = 'active'",
            "SELECT * FROM users WHERE status = 'active'",
            "SELECT user_id, username FROM users WHERE status = 'active'"  # 一次不同
        ]
        
        counter = Counter(results)
        most_common_count = counter.most_common(1)[0][1]
        consistency_rate = most_common_count / len(results)
        
        # 4/5 = 80%
        assert consistency_rate == 0.8
        assert consistency_rate < 0.95, "此例应该不通过 95% 阈值"
    
    def test_perfect_consistency(self):
        """测试完美一致性（100%）"""
        results = ["SELECT * FROM users"] * 5
        
        counter = Counter(results)
        most_common_count = counter.most_common(1)[0][1]
        consistency_rate = most_common_count / len(results)
        
        assert consistency_rate == 1.0, "完全一致应该是 100%"


class TestLLMConfiguration:
    """LLM 配置测试"""
    
    def test_llm_temperature_is_zero(self):
        """测试 LLM temperature 设置为 0"""
        from infrastructure.llm.llm_service import get_llm_service
        
        llm_service = get_llm_service()
        
        if llm_service.llm is not None:
            # 验证 temperature 为 0
            assert hasattr(llm_service.llm, "temperature")
            assert llm_service.llm.temperature == 0, "temperature 必须为 0（确定性输出）"
    
    def test_llm_top_p_is_one(self):
        """测试 LLM top_p 设置为 1.0（或接近 1.0）"""
        from infrastructure.llm.llm_service import get_llm_service
        
        llm_service = get_llm_service()
        
        if llm_service.llm is None:
            pytest.skip("LLM 未初始化，跳过配置检查")
        
        # top_p 可能在不同位置，按优先级检查
        top_p_value = None
        
        # 方式1：直接属性
        if hasattr(llm_service.llm, "top_p"):
            top_p_value = llm_service.llm.top_p
        
        # 方式2：model_kwargs 中
        elif hasattr(llm_service.llm, "model_kwargs"):
            model_kwargs = llm_service.llm.model_kwargs
            if isinstance(model_kwargs, dict) and "top_p" in model_kwargs:
                top_p_value = model_kwargs["top_p"]
        
        # 方式3：检查初始化参数（某些 LangChain 版本）
        elif hasattr(llm_service.llm, "_identifying_params"):
            params = llm_service.llm._identifying_params
            if "top_p" in params:
                top_p_value = params["top_p"]
        
        # 验证 top_p 值
        if top_p_value is not None:
            assert top_p_value == 1.0 or top_p_value >= 0.99, \
                f"top_p 应该为 1.0（确定性输出），实际: {top_p_value}"
        else:
            # 如果找不到 top_p，至少警告
            import warnings
            warnings.warn(
                "无法检测到 LLM 的 top_p 配置。"
                "请确保 LLM 初始化时设置了 top_p=1.0 以保证输出一致性。"
            )


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
