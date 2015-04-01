/**
 *
 * APDPlat - Application Product Development Platform Copyright (c) 2013, 杨尚川,
 * yang-shangchuan@qq.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.apdplat.superword.tools;

import org.apache.commons.lang.StringUtils;
import org.apdplat.superword.model.SynonymAntonym;
import org.apdplat.superword.model.SynonymDiscrimination;
import org.apdplat.superword.model.Word;
import org.apdplat.superword.rule.PartOfSpeech;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * HTML格式化工具，将生成的HTML片段发布到网络上的博客、日志中
 * @author 杨尚川
 */
public class HtmlFormatter {
    private HtmlFormatter(){}
    private static final String RED_EM_PRE = "<span style=\"color:red\">";
    private static final String RED_EM_SUF = "</span>";
    private static final String BLUE_EM_PRE = "<span style=\"color:blue\">";
    private static final String BLUE_EM_SUF = "</span>";

    public static String toHtmlForCompoundWord(Map<Word, Map<Integer, List<Word>>> data){
        Set<Word> elements = new HashSet<>();
        StringBuilder html = new StringBuilder();
        html.append("<table  border=\"1\">\n");
        AtomicInteger i = new AtomicInteger();
        data
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .forEach(entry -> {
                    html.append("\t<tr><td>")
                            .append(i.incrementAndGet())
                            .append("</td><td>")
                            .append(WordLinker.toLink(entry.getKey().getWord()))
                            .append("</td>");
                    entry
                            .getValue()
                            .values()
                            .forEach(words -> {
                                words.forEach(word -> {
                                    html.append("<td>")
                                            .append(WordLinker.toLink(word.getWord()))
                                            .append("</td>");
                                    elements.add(word);
                                });
                            });
                    html.append("</tr>\n");
                });
        html.append("</table>\n");

        if(elements.isEmpty()){
            return html.toString();
        }

        html.append("\n不重复的被组合词有：")
                .append(elements.size())
                .append("个，分别是：<br/>\n");

        List<String> words = elements
                .stream()
                .sorted()
                .map(word -> WordLinker.toLink(word.getWord()))
                .collect(Collectors.toList());
        html.append(toHtmlTableFragment(words, 5));

        return html.toString();
    }

    public static String toHtmlForPartOfSpeech(Map<String, Set<String>> data){
        StringBuilder html = new StringBuilder();
        html.append("<h4>各大词性广泛度排名：</h4><br/>\n");
        AtomicInteger i = new AtomicInteger();
        data.entrySet().stream().sorted((a,b)->b.getValue().size()-a.getValue().size()).forEach(e -> {
            String k = e.getKey();
            html.append(i.incrementAndGet())
                    .append("、")
                    .append(RED_EM_PRE)
                    .append(k)
                    .append(RED_EM_SUF)
                    .append("(")
                    .append(BLUE_EM_PRE)
                    .append(PartOfSpeech.getMeaning(k))
                    .append(BLUE_EM_SUF)
                    .append(") (词数:")
                    .append(data.get(k).size())
                    .append(")")
                    .append("<br/>\n");

        });
        html.append("<h4>各大词性及其包括的词：</h4><br/>\n");
        AtomicInteger j = new AtomicInteger();
        data.keySet().stream().sorted().forEach(k -> {
            html.append("<h4>")
                    .append(j.incrementAndGet())
                    .append("、")
                    .append(RED_EM_PRE)
                    .append(k)
                    .append(RED_EM_SUF)
                    .append("(")
                    .append(BLUE_EM_PRE)
                    .append(PartOfSpeech.getMeaning(k))
                    .append(BLUE_EM_SUF)
                    .append(") (词数:")
                    .append(data.get(k).size())
                    .append(")")
                    .append("</h4>\n")
                    .append(
                            toHtmlTableFragment(data.get(k).stream().sorted().map(w -> WordLinker.toLink(w)).collect(Collectors.toList()), 5));
        });
        return html.toString();
    }

    public static String toHtmlForPluralFormat(Map<String, String> data){
        StringBuilder html = new StringBuilder();
        html.append("<table border=\"1\">\n")
            .append("\t<tr><td>单词原型</td><td>单词复数</td></tr>\n");
        data.keySet().forEach(key -> {
            String origin = key.substring(0, key.length() - data.get(key).length());
            html.append("\t<tr><td>").append(WordLinker.toLink(origin)).append("</td><td>")
                .append(WordLinker.toLink(key, origin, BLUE_EM_PRE, BLUE_EM_SUF + "-")).append("</td></tr>\n");

        });
        html.append("</table>\n");
        return html.toString();
    }

    public static String toHtmlForWordDefinition(Set<Word> words, int rowLength) {
        Map<Integer, AtomicInteger> map = new HashMap<>();
        words.stream().forEach(w -> {
            int count = w.getDefinitions().size();
            map.putIfAbsent(count, new AtomicInteger());
            map.get(count).incrementAndGet();
        });
        List<String> data =
                    words
                        .stream()
                        .sorted((a, b) -> b.getDefinitions().size() - a.getDefinitions().size())
                        .map(word -> WordLinker.toLink(word.getWord())+"-"+word.getDefinitions().size())
                        .collect(Collectors.toList());
        StringBuilder html = new StringBuilder();
        html.append(toHtmlTableFragment(data, rowLength))
            .append("<table border=\"1\">\n")
            .append("\t<tr><td>定义条数</td><td>单词个数</td></tr>\n");
        map.keySet().stream().sorted((a,b)->b-a).forEach(key -> {
            html.append("\t<tr><td>").append(key).append("</td><td>").append(map.get(key)).append("</td></tr>\n");
        });
        html.append("</table>\n");
        return html.toString();
    }

    public static String toHtmlForAntonym(Set<SynonymAntonym> synonymAntonyms, int rowLength){
        StringBuilder html = new StringBuilder();
        AtomicInteger i = new AtomicInteger();
        synonymAntonyms
                .stream()
                .sorted((a, b) -> b.getAntonym().size() - a.getAntonym().size())
                .forEach(sa -> {
                    if (!sa.getAntonym().isEmpty()) {
                        html.append("<h4>")
                                .append(i.incrementAndGet())
                                .append("、")
                                .append(WordLinker.toLink(sa.getWord().getWord()))
                                .append("</h4>\n")
                                .append("<b>反义词(")
                                .append(sa.getAntonym().size())
                                .append(")：</b><br/>\n");
                        List<String> sm = sa.getAntonym().stream().sorted().map(w -> WordLinker.toLink(w.getWord())).collect(Collectors.toList());
                        html.append(toHtmlTableFragment(sm, rowLength))
                            .append("<br/>\n");
                    }
                });
        return html.toString();
    }

    public static String toHtmlForSynonymAntonym(Set<SynonymAntonym> synonymAntonyms, int rowLength){
        StringBuilder html = new StringBuilder();
        AtomicInteger i = new AtomicInteger();
        synonymAntonyms
                .stream()
                .sorted((a, b) -> b.size() - a.size())
                .forEach(sa -> {
                    html.append("<h4>")
                            .append(i.incrementAndGet())
                            .append("、")
                            .append(WordLinker.toLink(sa.getWord().getWord()))
                            .append("</h4>\n");
                    if (!sa.getSynonym().isEmpty()) {
                        html.append("<b>同义词(").append(sa.getSynonym().size()).append(")：</b><br/>\n");
                        List<String> sm = sa.getSynonym().stream().sorted().map(w -> WordLinker.toLink(w.getWord())).collect(Collectors.toList());
                        html.append(toHtmlTableFragment(sm, rowLength));
                    }
                    if (!sa.getAntonym().isEmpty()) {
                        html.append("<b>反义词(").append(sa.getAntonym().size()).append(")：</b><br/>\n");
                        List<String> sm = sa.getAntonym().stream().sorted().map(w -> WordLinker.toLink(w.getWord())).collect(Collectors.toList());
                        html.append(toHtmlTableFragment(sm, rowLength));
                    }
                    html.append("<br/>\n");
                });
        return html.toString();
    }
    public static String toHtmlForSynonymDiscrimination(Set<SynonymDiscrimination> synonymDiscrimination){
        StringBuilder html = new StringBuilder();
        AtomicInteger i = new AtomicInteger();
        synonymDiscrimination
        .stream()
        .sorted()
        .forEach(sd -> {
                html.append("<h4>")
                    .append(i.incrementAndGet())
                    .append("、")
                    .append(sd.getTitle())
                    .append("</h4>\n<b>")
                    .append(sd.getDes().replace("“", "“" + BLUE_EM_PRE).replace("”", BLUE_EM_SUF +"”"))
                    .append("</b><br/>\n");
                if (!sd.getWords().isEmpty()) {
                    html.append("<ol>\n");
                }
                sd.getWords()
                    .forEach(w -> {
                        html.append("\t<li>")
                                .append(WordLinker.toLink(w.getWord()))
                                .append("：")
                                .append(w.getMeaning())
                                .append("</li>\n");
                    });
                if (!sd.getWords().isEmpty()) {
                    html.append("</ol>\n");
                }
        });
        return html.toString();
    }

    public static String toHtmlTableFragmentForRootAffix(Map<Word, List<Word>> rootAffixToWords, int rowLength) {
        StringBuilder html = new StringBuilder();
        AtomicInteger rootCounter = new AtomicInteger();
        Set<Word> unique = new HashSet<>();
        rootAffixToWords
        .keySet()
        .stream()
        .sorted()
        .forEach(rootAffix -> {
            List<Word> words = rootAffixToWords.get(rootAffix);
            html.append("<h4>")
                .append(rootCounter.incrementAndGet())
                .append("、")
                .append(rootAffix.getWord());
            if(StringUtils.isNotBlank(rootAffix.getMeaning())) {
                html.append(" (")
                    .append(rootAffix.getMeaning())
                    .append(") ");
            }
            html.append(" (hit ")
                .append(words.size())
                .append(")</h4></br>\n");
            List<String> data =
                    words
                        .stream()
                        .sorted()
                        .map(word -> {
                            unique.add(word);
                            return emphasize(word, rootAffix);
                        })
                        .collect(Collectors.toList());
            html.append(toHtmlTableFragment(data, rowLength));
        });
        String head = "词根词缀数："+rootAffixToWords.keySet().size()+"，单词数："+unique.size()+"<br/>\n";
        return head+html.toString();
    }

    public static String emphasize(Word word, Word rootAffix){
        String w = word.getWord();
        String r = rootAffix.getWord().replace("-", "").toLowerCase();
        //词就是词根
        if (w.length() == r.length()) {
            return WordLinker.toLink(w, r);
        }
        //词根在中间
        if (w.length() > r.length()
                && !w.startsWith(r)
                && !w.endsWith(r)) {
            return WordLinker.toLink(w, r, "-" + RED_EM_PRE, RED_EM_SUF + "-");
        }
        //词根在前面
        if (w.length() > r.length() && w.startsWith(r)) {
            return WordLinker.toLink(w, r, "" + RED_EM_PRE, RED_EM_SUF + "-");
        }
        //词根在后面面
        if (w.length() > r.length() && w.endsWith(r)) {
            return WordLinker.toLink(w, r, "-" + RED_EM_PRE, RED_EM_SUF + "");
        }
        return WordLinker.toLink(w, r);
    }

    public static String toHtmlTableFragment(Map<Word, AtomicInteger> words, int rowLength) {
        return toHtmlTableFragment(words.entrySet(), rowLength);
    }
    public static String toHtmlTableFragment(Set<Map.Entry<Word, AtomicInteger>> words, int rowLength) {

        List<String> data =
        words
            .stream()
            .sorted((a, b) -> b.getValue().get() - a.getValue().get())
            .map(entry -> {
                String link = WordLinker.toLink(entry.getKey().getWord());
                if (entry.getValue().get() > 0) {
                    link = link+"-"+entry.getValue().get();
                }
                return link;
            })
            .collect(Collectors.toList());

        return toHtmlTableFragment(data, rowLength);
    }

    public static List<String> toHtmlTableFragmentForIndependentWord(Map<Word, List<Word>> data, int rowLength, int wordsLength) {
        List<String> htmls = new ArrayList<>();
        StringBuilder html = new StringBuilder();
        AtomicInteger wordCounter = new AtomicInteger();
        data
            .keySet()
            .stream()
            .sorted()
            .forEach(word -> {
                html.append("<h4>")
                        .append(wordCounter.incrementAndGet())
                        .append("、")
                        .append(word.getWord())
                        .append(" (form ")
                        .append(data.get(word).size())
                        .append(")</h4></br>\n");
                List<String> result = data
                        .get(word)
                        .stream()
                        .map(rootAffix -> emphasize(word, rootAffix))
                        .collect(Collectors.toList());
                html.append(toHtmlTableFragment(result, rowLength));
                result.clear();
                result = data
                        .get(word)
                        .stream()
                        .flatMap(rootAffix -> Arrays.asList(rootAffix.getWord(), rootAffix.getMeaning()).stream())
                        .collect(Collectors.toList());
                html.append(toHtmlTableFragment(result, 2));
                result.clear();
                if (wordCounter.get() % wordsLength == 0) {
                    htmls.add(html.toString());
                    html.setLength(0);
                }
            });
        if(html.length() > 0){
            htmls.add(html.toString());
        }
        return htmls;
    }

    public static String toHtmlTableFragment(List<String> data, int rowLength) {
        StringBuilder html = new StringBuilder();

        AtomicInteger rowCounter = new AtomicInteger();
        AtomicInteger wordCounter = new AtomicInteger();
        html.append("<table  border=\"1\">\n");
        data
            .forEach(datum -> {
                if (wordCounter.get() % rowLength == 0) {
                    if (wordCounter.get() == 0) {
                        html.append("\t<tr>");
                    } else {
                        html.append("</tr>\n\t<tr>");
                    }
                    rowCounter.incrementAndGet();
                    html.append("<td>").append(rowCounter.get()).append("</td>");
                }
                wordCounter.incrementAndGet();
                html.append("<td>").append(datum).append("</td>");
            });
        if(html.toString().endsWith("<tr>")){
            html.setLength(html.length()-5);
        }else{
            html.append("</tr>\n");
        }
        html.append("</table>\n");

        return html.toString();
    }
}
