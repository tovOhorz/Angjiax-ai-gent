package com.angjiax.angjiaxaigent.demo.rag;

import com.knuddels.jtokkit.api.EncodingType;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class EmbeddingConfig {

    /* TODO:嵌入大量文档优化
    在使用向量存储时，可能要嵌入大量文档，如果一次性处理存储大量文档，可能会导致性能问题、甚至出现错误
    导致数据不完整。
    举个例子，嵌入模型一般有一个最大标记限制，通常称为上下文窗口大小（context window size），限制了单个嵌
    入请求中可以处理的文本量。如果在一次调用中转换过多文档可能直接导致报错。
    为此，SpringAl实现了批处理策略（BatchingStrategy)，将大量文档分解为较小的批次，使其适合嵌入模型的最
    大上下文窗口，还可以提高性能并更有效地利用API速率限制。
    Spring Al通过BatchingStrategy接口提供该功能，该接口允许基于文档的标记计数并以分批方式处理文档：
    *
    复制代码
    public interface BatchingStrategy{
    List<List<Document>> batch(List<Document> documents);
    }
    该接口定义了一个单一方法batch，它接收一个文档列表并返回一个文档批次列表。
    Spring Al提供了一个名为TokenCountBatchingStrategy的默认实现。这个策略为每个文档估算token数，将文档
    分组到不超过最大输入token数的批次中，如果单个文档超过此限制，则抛出异常。这样就确保了每个批次不超
    过计算出的最大输入token数。
    * */
    //@Bean
    public BatchingStrategy customTokenCountBatchingStrategy() {
        return new TokenCountBatchingStrategy(
                EncodingType.CL100K_BASE, //设定编码类型
                8000, //设置最大输出标记计数
                0.1    //设置保留百分比
        );
    }
}
