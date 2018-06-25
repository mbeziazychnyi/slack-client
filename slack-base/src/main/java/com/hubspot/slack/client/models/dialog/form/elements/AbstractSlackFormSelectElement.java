package com.hubspot.slack.client.models.dialog.form.elements;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.immutables.value.Value.Check;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hubspot.immutables.style.HubSpotStyle;
import com.hubspot.slack.client.models.actions.SlackDataSource;
import com.hubspot.slack.client.models.dialog.form.SlackFormElementTypes;

@Immutable
@HubSpotStyle
@JsonNaming(SnakeCaseStrategy.class)
@JsonInclude(Include.NON_EMPTY)
public abstract class AbstractSlackFormSelectElement extends SlackDialogFormElement {
  @Default
  @Override
  public SlackFormElementTypes getType() {
    return SlackFormElementTypes.SELECT;
  }

  @Default
  public SlackDataSource getDataSource() {
    return SlackDataSource.STATIC;
  }

  public abstract List<SlackFormOption> getOptions();
  public abstract List<SlackFormOptionGroup> getOptionGroups();
  public abstract Optional<String> getValue();
  public abstract List<SlackFormOption> getSelectedOptions();
  public abstract Optional<Integer> getMinQueryLength();

  @Check
  public void validate() {
    super.validateBaseElementProperties();

    int numOptions = getOptions().size();
    int numOptionGroups = getOptionGroups().size();

    if (numOptions > 100) {
      throw new IllegalStateException("Cannot have more than 100 options");
    }

    if (numOptionGroups > 100) {
      throw new IllegalStateException("Cannot have more than 100 option groups");
    }

    if (getDataSource().equals(SlackDataSource.STATIC)) {
      if (numOptions == 0 && numOptionGroups == 0) {
        throw new IllegalStateException("Either options or option groups are required for static data source types");
      }
    }

    if (getValue().isPresent() && getDataSource().equals(SlackDataSource.EXTERNAL)) {
      throw new IllegalStateException("Cannot use value for external data source, must use selected options");
    }

    if (getValue().isPresent()) {
      boolean valueIsSomeOptionValue = getOptions().stream()
          .anyMatch(option -> option.getValue().equalsIgnoreCase(getValue().get()));
      if (!valueIsSomeOptionValue) {
        throw new IllegalStateException("Value must exactly match the value field for one provided option");
      }
    }

    if (!getSelectedOptions().isEmpty()) {
      if (getSelectedOptions().size() != 1) {
        throw new IllegalStateException("Selected options must be a single element array");
      }
      boolean selectedOptionIsInOptionsGroup = getOptionGroups().stream()
          .map(SlackFormOptionGroup::getOptions)
          .collect(Collectors.toList())
          .stream()
          .flatMap(List::stream)
          .anyMatch(option -> option.equals(getSelectedOptions().get(0)));
      if (!selectedOptionIsInOptionsGroup) {
        throw new IllegalStateException("Selected option must exactly match an option in the options groups");
      }
    }
  }
}
