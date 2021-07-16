/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.internal.util.redaction;

import static org.apache.geode.internal.util.redaction.SensitiveDataRegex.getPattern;
import static org.apache.geode.internal.util.redaction.SensitiveDataRegex.getRegex;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import org.apache.geode.internal.util.redaction.SensitiveDataRegex.Group;

/**
 * SensitiveDataRegex parses strings that match GFSH syntax or system property key/value pairs.
 *
 * <p>
 * GFSH syntax allows command options:
 *
 * <pre>
 * option: "option"
 * option equals argument: "option=argument"
 * option space(s) argument: "option argument"
 * </pre>
 *
 * <p>
 * GFSH option requires leading hyphens:
 *
 * <pre>
 * leading hyphens: "--option=argument"
 * leading hyphensJD: "--J=-Doption=argument"
 * </pre>
 *
 * <p>
 * GFSH option without a argument ("option") has an implicit value of "true".
 *
 * <p>
 * System property syntax:
 *
 * <pre>
 * "property=value"
 * "-Dproperty=value"
 * </pre>
 *
 * <p>
 * See {@code SensitiveDataRegexTest} for lots of examples.
 *
 * <p>
 * Terms used in tests:
 *
 * <pre>
 * hyphen: "-"
 * hyphens: "--" or "---"
 * hyphenD: "-D"
 * hyphensJD: "--J=-D"
 * </pre>
 */
public class SensitiveDataRegexTest {

  @Test
  public void regex() {
    System.out.println("regex: " + getRegex());
  }

  @Test
  public void hasFourCaptureGroups() {
    assertThat(Group.values()).hasSize(4);
  }

  @Test
  public void captures_hyphenDOption_equalsArgument() {
    String input = "-Doption=argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("-D");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("argument");
  }

  @Test
  public void captures_hyphensJDOption_equalsArgument() {
    String input = "--J=-Doption=argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--J=-D");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("argument");
  }

  @Test
  public void captures_hyphensOption_spaceEqualsArgument() {
    String input = "--option =argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo(" =");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("argument");
  }

  @Test
  public void captures_hyphensOption_equalsSpaceArgument() {
    String input = "--option= argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("= ");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("argument");
  }

  @Test
  public void captures_hyphensOption_spaceEqualsSpaceArgument() {
    String input = "--option = argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo(" = ");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("argument");
  }

  @Test
  public void captures_hyphensOptionContainingHyphens_equalsArgument() {
    String input = "--this-is-the-option=argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("this-is-the-option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("argument");
  }

  @Test
  public void captures_hyphensOption_equalsArgumentContainingHyphen() {
    String input = "--option=the-argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("the-argument");
  }

  @Test
  public void captures_hyphensOption_equalsArgumentContainingHyphens() {
    String input = "--option=this-is-the-argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("this-is-the-argument");
  }

  @Test
  public void captures_hyphensOption_equalsArgument() {
    String input = "--option=argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("argument");
  }

  @Test
  public void captures_hyphensOption_spaceArgument() {
    String input = "--option argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo(" ");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("argument");
  }

  @Test
  public void captures_hyphensOption_twoSpacesArgument() {
    String input = "--option  argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("  ");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("argument");
  }

  @Test
  public void captures_hyphensOption_threeSpacesArgument() {
    String input = "--option   argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("   ");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("argument");
  }

  @Test
  public void captures_hyphensOption_equalsHyphenArgument() {
    String input = "--option=-argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("-argument");
  }

  @Test
  public void captures_hyphensOption_spaceHyphenArgument() {
    String input = "--option -argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo(" ");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("-argument");
  }

  @Test
  public void captures_hyphensOption_equalsHyphenArgumentInQuotes() {
    String input = "--option=\"-argument\"";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("\"-argument\"");
  }

  @Test
  public void captures_twoHyphensOption_equalsHyphensArgument_niceToFix() {
    String input = "--option=--argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("--argument");
  }

  @Test
  public void doesNotMatch_twoHyphensOption_spaceHyphensArgument() {
    String input = "--option --argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isFalse();
  }

  @Test
  public void captures_twoHyphensOption_equalsHyphensArgumentInQuotes() {
    String input = "--option=\"--argument\"";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("\"--argument\"");
  }

  @Test
  public void captures_threeHyphensOption_equalsHyphensArgument_niceToFix() {
    String input = "--option=---argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("---argument");
  }

  @Test
  public void doesNotMatch_threeHyphensOption_spaceHyphensArgument() {
    String input = "--option ---argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isFalse();
  }

  @Test
  public void captures_hyphensOption_equalsManyHyphensArgumentInQuotes() {
    String input = "--option=\"---argument\"";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("\"---argument\"");
  }

  @Test
  public void doesNotCapture_hyphensOption_equalsHyphenArgumentContainingSpaceWithHyphen() {
    String input = "--option=-foo -bar";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo(" ");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("-bar");
  }

  @Test
  public void captures_hyphensOption_equalsHyphenArgumentContainingSpaceWithHyphenInQuotes() {
    String input = "--option=\"-foo -bar\"";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("\"-foo -bar\"");
  }

  @Test
  public void doesNotMatch_hyphensOption_equalsTwoHyphensArgumentContainingSpaceWithTwoHyphens() {
    String input = "--option=--foo --bar";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isFalse();
  }

  @Test
  public void captures_hyphensOption_equalsTwoHyphensArgumentContainingSpaceWithTwoHyphensInQuotes() {
    String input = "--option=\"--foo --bar\"";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("\"--foo --bar\"");
  }

  @Test
  public void captures_hyphensOptionContainingUnderlines_equalsArgument() {
    String input = "--this_is_the_option=argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("this_is_the_option");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("argument");
  }

  @Test
  public void matches_hyphensOption_equalsArgumentContainingUnderlines() {
    String input = "--option=this_is_the_argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
  }

  @Test
  public void matches_hyphensOption_equalsUnderlineArgument() {
    String input = "--option=_argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
  }

  @Test
  public void matches_hyphensOption_spaceUnderlineArgument() {
    String input = "--option _argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
  }

  @Test
  public void matches_hyphensOption_equalsUnderlinesArgument() {
    String input = "--option=___argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
  }

  @Test
  public void matches_hyphensOption_spaceUnderlinesArgument() {
    String input = "--option ___argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
  }

  @Test
  public void matches_hyphensOptionContainingPeriods_equalsArgument() {
    String input = "--this.is.the.option=argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
  }

  @Test
  public void matches_hyphensOption_equalsArgumentContainingPeriods() {
    String input = "--option=this.is.the.argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
  }

  @Test
  public void matches_hyphensOption_equalsPeriodArgument() {
    String input = "--option=.argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
  }

  @Test
  public void matches_hyphensOption_spacePeriodArgument() {
    String input = "--option .argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
  }

  @Test
  public void matches_hyphensOption_equalsPeriodsArgument() {
    String input = "--option=...argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
  }

  @Test
  public void matches_hyphensOption_spacePeriodsArgument() {
    String input = "--option ...argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isTrue();
  }

  @Test
  public void groupZero_capturesInput() {
    String input = "--option=argument";
    Matcher matcher = getPattern().matcher(input);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group(0)).isEqualTo(input);
  }

  @Test
  public void groupValues_hasSizeEqualToGroupCount() {
    String input = "--option=argument";
    Matcher matcher = getPattern().matcher(input);
    assertThat(matcher.matches()).isTrue();

    assertThat(Group.values()).hasSize(matcher.groupCount());
  }

  @Test
  public void groupPrefix_capturesHyphens_beforeOption() {
    String prefix = "--";
    String input = prefix + "option=argument";
    Matcher matcher = getPattern().matcher(input);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo(prefix);
  }

  @Test
  public void doesNotMatch_singleHyphen() {
    String input = "-option=argument";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.matches()).isFalse();
  }

  @Test
  public void groupPrefix_capturesHyphenD() {
    String prefix = "-D";
    String input = prefix + "option=argument";
    Matcher matcher = getPattern().matcher(input);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo(prefix);
  }

  @Test
  public void groupPrefix_capturesHyphenJD() {
    String prefix = "--J=-D";
    String input = prefix + "option=argument";
    Matcher matcher = getPattern().matcher(input);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo(prefix);
  }

  @Test
  public void groupOption_capturesOption_excludingHyphens() {
    String option = "option";
    String input = "--" + option + "=argument";
    Matcher matcher = getPattern().matcher(input);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo(option);
  }

  @Test
  public void groupAssignment_capturesEquals() {
    String assignment = "=";
    String input = "--option" + assignment + "argument";
    Matcher matcher = getPattern().matcher(input);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo(assignment);
  }

  @Test
  public void groupArgument_capturesArgument() {
    String argument = "argument";
    String input = "--option=" + argument;
    Matcher matcher = getPattern().matcher(input);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo(argument);
  }

  @Test
  public void groupPrefix_capturesIsolatedHyphens() {
    String prefix = "--";
    Matcher matcher = Pattern.compile(Group.PREFIX.getRegex()).matcher(prefix);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group()).isEqualTo(prefix);
  }

  @Test
  public void groupOption_capturesIsolatedOption() {
    String option = "option";
    Matcher matcher = Pattern.compile(Group.OPTION.getRegex()).matcher(option);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group()).isEqualTo(option);
  }

  @Test
  public void groupAssignment_capturesIsolatedEquals() {
    String assignment = " = ";
    Matcher matcher = Pattern.compile(Group.ASSIGNMENT.getRegex()).matcher(assignment);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group()).isEqualTo(assignment);
  }

  @Test
  public void groupArgument_capturesIsolatedArgument() {
    String argument = "argument";
    Matcher matcher = Pattern.compile(Group.ARGUMENT.getRegex()).matcher(argument);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group()).isEqualTo(argument);
  }

  @Test
  public void groupArgument_capturesIsolatedHyphensArgument() {
    String argument = "---argument";
    Matcher matcher = Pattern.compile(Group.ARGUMENT.getRegex()).matcher(argument);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group()).isEqualTo(argument);
  }

  @Test
  public void groupArgument_doesNotMatchIsolatedArgumentContainingSpaces() {
    String argument = "foo bar oi vey";
    Matcher matcher = Pattern.compile(Group.ARGUMENT.getRegex()).matcher(argument);

    assertThat(matcher.matches()).isFalse();
  }

  @Test
  public void groupArgument_doesNotMatchIsolatedArgumentContainingHyphensAndSpaces() {
    String argument = "--foo --bar --oi --vey";
    Matcher matcher = Pattern.compile(Group.ARGUMENT.getRegex()).matcher(argument);

    assertThat(matcher.matches()).isFalse();
  }

  @Test
  public void groupArgument_capturesIsolatedArgumentContainingHyphensAndSpaces_inQuotes() {
    String argument = "\"--foo --bar --oi --vey\"";
    Matcher matcher = Pattern.compile(Group.ARGUMENT.getRegex()).matcher(argument);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group()).isEqualTo(argument);
  }

  @Test
  public void groupArgument_capturesIsolatedArgumentWithTrailingHyphen() {
    String argument = "seekrit-";
    Matcher matcher = Pattern.compile(Group.ARGUMENT.getRegex()).matcher(argument);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group()).isEqualTo(argument);
  }

  @Test
  public void groupArgument_capturesIsolatedArgumentWithTrailingQuote() {
    String argument = "seekrit\"";
    Matcher matcher = Pattern.compile(Group.ARGUMENT.getRegex()).matcher(argument);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group()).isEqualTo(argument);
  }

  @Test
  public void groupArgument_capturesIsolatedArgumentContainingSymbols() {
    String argument = "'s#kr!\"t";
    Matcher matcher = Pattern.compile(Group.ARGUMENT.getRegex()).matcher(argument);
    assertThat(matcher.matches()).isTrue();

    assertThat(matcher.group()).isEqualTo("'s#kr!\"t");
  }

  // @Test
  // public void boundarySpaceOrEquals() {
  // String argument = " ";
  // Matcher matcher = Pattern.compile(Boundary.SPACE_OR_EQUALS.getRegex()).matcher(argument);
  // assertThat(matcher.matches()).isTrue();
  //
  // assertThat(matcher.group()).isEqualTo(" ");
  //
  // }

  @Test
  public void captures_multipleOptionsWithDifferentPrefixes() {
    String input = "-DmyArg -Duser-password=foo --classpath=.";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.find()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("-D");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("myArg");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isNull();
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isNull();

    assertThat(matcher.find()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("-D");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("user-password");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("foo");

    assertThat(matcher.find()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("classpath");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo(".");
  }

  @Test
  public void captures_multipleOptionsStartingWithTwoHyphens_afterCommand() {
    String input = "connect --password=foo --user=bar";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.find()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("password");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("foo");

    assertThat(matcher.find()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("user");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("bar");
  }

  @Test
  public void captures_multipleOptionsStartingWithTwoHyphens() {
    String input = "--option1=argument1 --option2=argument2";
    Matcher matcher = getPattern().matcher(input);

    assertThat(matcher.find()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option1");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("argument1");

    assertThat(matcher.find()).isTrue();
    assertThat(matcher.group(Group.PREFIX.getIndex())).isEqualTo("--");
    assertThat(matcher.group(Group.OPTION.getIndex())).isEqualTo("option2");
    assertThat(matcher.group(Group.ASSIGNMENT.getIndex())).isEqualTo("=");
    assertThat(matcher.group(Group.ARGUMENT.getIndex())).isEqualTo("argument2");
  }
}
