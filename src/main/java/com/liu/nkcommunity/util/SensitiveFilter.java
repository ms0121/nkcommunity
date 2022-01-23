package com.liu.nkcommunity.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    // 记录日记
    private static final Logger LOGGER = LoggerFactory.getLogger(SensitiveFilter.class);

    // 根节点
    private TrieNode rootNode = new TrieNode();

    // 敏感词替换词
    private static final String REPLACEMENT = "***";

    // 表示当前的方法在该类 SensitiveFilter 被加载(容器启动的时候)，
    // 和调用构造器方法后会被执行一次
    @PostConstruct
    public void init() {
        try (
                // 使用字节流读取文件
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                // 将字节流转为字符流
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 将当前行读取到的敏感词添加到前缀树当中
                this.addKeyword(keyword);
            }
        } catch (Exception e) {
            LOGGER.error("加载敏感词文件失败: " + e.getMessage());
        }
    }


    // 将一个敏感词添加到当前的前缀树中去
    private void addKeyword(String keyword) {
        // 设置临时节点指向根节点
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            // 遍历每个字符，并判断当前的字符是否存在
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                // 初始化一个新的子节点
                subNode = new TrieNode();
                // 然后挂在到tempNode节点中
                tempNode.addSubNode(c, subNode);
            }
            // 指向子节点，进入下一轮循环
            tempNode = subNode;
            // 设置当前敏感词的结束标志
            if (i == keyword.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    // 过滤敏感词语对外接口
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }
        // 定义指向前缀树的指针
        TrieNode tempNode = rootNode;
        // 指针2和3都指向文本数据
        int begin = 0, position = 0;
        // 记录最终的结果
        StringBuilder builder = new StringBuilder();

        while (position < text.length()){
            char c = text.charAt(position);

            // 跳过字符(特殊字符，A，B，1，2除外)
            if (isSymbol(c)){
                // 若指针1处于根节点，将此符号计入结果，让指针2向下走一步
                if (tempNode == rootNode){
                    builder.append(c);
                    begin++;
                }
                // 无论符号是在开头或中间，指针3都向下走一步
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null){
                // 以begin开头的字符串不是敏感词，然后将当前的字符添加过结果集中
                builder.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重新指回到根节点
                tempNode = rootNode;
            }else if (tempNode.isKeywordEnd()){
                // 发现了敏感词，将begin ~ position的字符串进行替换掉
                builder.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = rootNode;
            }else {
                // 检查下一个字符
                position++;
            }
        }
        // 将最后一批字符计入结果中
        builder.append(text.substring(begin));
        // 返回结果集
        return builder.toString();
    }

    // 判断当前的字符是否是特殊字符
    private boolean isSymbol(Character c){
        // 0x2E80 ~ 0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // 前缀树节点类型，用于过滤敏感的字符
    private class TrieNode {

        // 关键词结束标志
        private boolean isKeywordEnd = false;

        // 子节点(key是下级字符，value是下级节点)
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode trieNode) {
            subNodes.put(c, trieNode);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

    }


}
