//package com.intrafind.semantic.queryprocessing.processor;
//
//import java.lang.reflect.Type;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonDeserializationContext;
//import com.google.gson.JsonDeserializer;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParseException;
//import com.google.gson.reflect.TypeToken;
//import com.intrafind.api.search.Search;
//import com.intrafind.common.annotations.VisibleForTesting;
//import com.intrafind.common.config.Configs;
//import com.intrafind.common.functional.processor.Exchange;
//import com.intrafind.common.json.Json;
//import com.intrafind.common.logging.Log;
//import com.intrafind.common.script.Script;
//import com.intrafind.common.string.Strings;
//import com.intrafind.common.util.Maps;
//import com.intrafind.common.util.Pair;
//
//public class TemplateQueriesProcessor implements QueryProcessor {
//
//  public  static final String  TEMPLATEQUERIES_TEMPLATES = "queryinterpreter.templatequeries.templates";
//  private static final Log     LOG                       = Log.getLog();
//  private static final Pattern MUSTACHE                  = Pattern.compile("\\{\\{(.*?)\\}\\}", Pattern.DOTALL); //dotall allows us to use newlines in the configuration
//
//  private static volatile Map<String, Template> templates;
//
//  static {
//
//    Configs.addListener(() -> {
//
//      try {
//        GsonBuilder gsonBldr = new GsonBuilder();
//        gsonBldr.registerTypeAdapter(Template.class, new TemplateDeserializer());
//        String templateString = Configs.CFG.getStr("*" + TEMPLATEQUERIES_TEMPLATES);
//        LOG.trace("queryinterpreter.templatequeries.templates: %s", templateString);
//        templates = gsonBldr.create().fromJson(templateString, new TypeToken<Map<String, Template>>() {}.getType());
//
//      } catch (Exception e) {
//
//        LOG.fatal(Strings.format("Exception reading config: %s", TEMPLATEQUERIES_TEMPLATES));
//        LOG.fatal(e);
//      }
//
//    });
//
//  }
//
//  static class Template {
//
//    public static final String ACTION     = "action";
//    public static final String FILTERNAME = "filtername";
//    public static final String FILTER     = "filter";
//
//    public Pattern             pattern;
//    public Map<String, String> features;
//
//    Template(String pattern, Map<String, String> features) {
//      this.pattern = Pattern.compile(pattern);
//      this.features = features;
//    }
//
//  }
//
//  static class TemplateDeserializer implements JsonDeserializer<Template> {
//
//    @Override
//    public Template deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
//      JsonObject          jObject  = jsonElement.getAsJsonObject();
//      String              pattern  = jObject.get("pattern").getAsString();
//      Map<String, String> features = Json.fromJson(jObject.get("features").toString());
//      return new Template(pattern, features);
//    }
//  }
//
//  @Override
//  public void process(Exchange exchange) {
//
//    String query = exchange.getStr(QueryProcessor.QUERY);
//
//    Map<String, Matcher> matchingConcepts = findConceptMatches(query);
//
//    if (matchingConcepts.isEmpty()) return;
//
//    try {
//
//      String newQuery = buildQuery(query, matchingConcepts);
//
//      Map<String, String> filters = buildFilters(matchingConcepts);
//
//      exchange.set(QueryProcessor.QUERY, newQuery);
//      exchange.set(QueryProcessor.EXPLAIN, String.format(Locale.GERMANY, "Query modified according to the following matching templates: %s", String.valueOf(matchingConcepts.keySet())));
//
//      for (Map.Entry<String, String> entry : filters.entrySet())
//        exchange.set(entry.getKey(), entry.getValue());
//
//    } catch (Exception e) {
//      LOG.debug("Exception processing query '%s'. Original query will be passed on.", query);
//      LOG.debug(e);
//    }
//
//  }
//
//  private Map<String, Matcher> findConceptMatches(String query) {
//
//    Map<String, Matcher> matchingConcepts = Maps.newMap();
//
//    if (templates.isEmpty()) return matchingConcepts;
//
//    for (Entry<String, Template> concept : templates.entrySet()) {
//      Matcher matcher = concept.getValue().pattern.matcher(Strings.toLower(query));
//
//      if (matcher.find()) {
//        matchingConcepts.put(concept.getKey(), matcher);
//      }
//    }
//
//    return onlyLongestMatches(matchingConcepts);
//
//  }
//
//  private Map<String, Matcher> onlyLongestMatches(Map<String, Matcher> matchingConcepts) {
//
//    List<Entry<String, Matcher>> entries = matchingConcepts.entrySet().stream()
//            .sorted((e1, e2) -> {
//              int startOffsetComparison = e1.getValue().start() - e2.getValue().start();
//              if (startOffsetComparison == 0)
//                return -(e1.getValue().end() - e2.getValue().end());
//              else
//                return startOffsetComparison;
//            }).collect(Collectors.toList());
//
//    Map<String, Matcher> result  = new HashMap<>();
//    int                  lastEnd = 0;
//    for (Entry<String, Matcher> e : entries) {
//      if (e.getValue().start() >= lastEnd) result.put(e.getKey(), e.getValue());
//      lastEnd = e.getValue().end();
//    }
//
//    return result;
//
//  }
//
//  private String buildQuery(String query, Map<String, Matcher> matchingConcepts) {
//
//    List<Pair<Integer, Integer>> list = matchingConcepts.values().stream()
//            .map(matcher -> Pair.of(matcher.start(), matcher.end()))
//            .sorted(Comparator.comparingInt(Pair::getFirst))
//            .collect(Collectors.toList());
//
//    StringBuilder newQuery = new StringBuilder();
//    int           i        = 0;
//
//    for (Pair<Integer, Integer> span : list) {
//      if (i > span.getFirst()) continue;
//      newQuery.append(query.subSequence(i, span.getFirst()));
//      i = span.getSecond();
//    }
//
//    newQuery.append(query.substring(i));
//
//    String finalQuery = newQuery.toString().replaceAll("\\s{2,}", " ").trim();
//
//    return null == finalQuery || finalQuery.isEmpty() ? "*" : finalQuery;
//  }
//
//  private Map<String, String> buildFilters(Map<String, Matcher> matchingConcepts) {
//
//    Map<String, String> filters = Maps.newMap();
//
//    for (Entry<String, Matcher> concept : matchingConcepts.entrySet()) {
//
//      Map<String, String> features = templates.get(concept.getKey()).features;
//
//      if (Template.FILTER.equals(features.get(Template.ACTION))) {
//        String filterValue = prepareFilter(features.get(Template.FILTER), concept.getValue());
//        filters.put(buildKey(filters, features.get(Template.FILTERNAME), filterValue), filterValue);
//      }
//
//    }
//
//    return filters;
//  }
//
//  @VisibleForTesting
//  static String prepareFilter(String filter, Matcher queryMatcher) {
//
//    Matcher matcher = MUSTACHE.matcher(filter);
//
//    StringBuilder newFilter = new StringBuilder();
//    int           i         = 0;
//
//    while (matcher.find()) {
//
//      newFilter.append(filter, i, matcher.start());
//
//      String value = Script.eval(matcher.group(1), "m", queryMatcher);
//
//      if (null == value)
//        throw new RuntimeException(
//                Strings.format("Evaluation of %s returned null. Make sure the script in your filter expression (%s) returns a valid filter.", matcher.group(1), TEMPLATEQUERIES_TEMPLATES));
//
//      newFilter.append(value);
//      i = matcher.end();
//
//    }
//
//    newFilter.append(filter.substring(i));
//
//    return newFilter.toString().trim();
//  }
//
//  @VisibleForTesting
//  static String buildKey(Map<String, String> filters, String filterName, String filterValue) {
//
//    // filter.query.intent.[filtername]
//    String key = Strings.format("%s.%s.%s", Search.FILTER_QUERY, QueryProcessor.INTENT_FILTER, filterName);
//
//    if (!filters.containsKey(key) || filterValue.equals(filters.get(key))) return key;
//
//    int j = 1;
//    while (true) {
//      String newKey = key.replaceAll("\\.?\\d*$", Strings.format(".%d", j++));
//      if (!filters.containsKey(newKey) || filterValue.equals(filters.get(newKey))) return newKey;
//    }
//  }
//
//}