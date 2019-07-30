import React, { Component } from "react";
import {
  View,
  TouchableHighlight,
  Text,
  StyleSheet,
  Platform,
  Animated
} from "react-native";
import PropTypes from "prop-types";
import { RectButton } from "react-native-gesture-handler";
import Swipeable from "react-native-gesture-handler/Swipeable";

class ListItem extends Component {
  static propTypes = {
    leftElement: PropTypes.element,
    title: PropTypes.string,
    description: PropTypes.string,
    rightElement: PropTypes.element,
    rightText: PropTypes.string,
    onPress: PropTypes.func,
    onDelete: PropTypes.func,
    onLongPress: PropTypes.func,
    disabled: PropTypes.bool
  };

  renderRightAction = (iconName, color, x, progress) => {
    const trans = progress.interpolate({
      inputRange: [0, 1],
      outputRange: [x, 0]
    });

    const pressHandler = () => {
      const { onDelete } = this.props;
      if (onDelete) onDelete();
      this.close();
    };

    return (
      <Animated.View style={{ flex: 1, transform: [{ translateX: trans }] }}>
        <RectButton
          style={[styles.rightAction, { backgroundColor: color }]}
          onPress={pressHandler}
        >
          <Text style={{ color: "#fff" }}>Delete</Text>
        </RectButton>
      </Animated.View>
    );
  };

  renderRightActions = progress => (
    <View style={{ width: 64, flexDirection: "row" }}>
      {this.renderRightAction("trash", "#ef5350", 64, progress)}
    </View>
  );

  renderRightActions = progress => (
    <View style={{ width: 64, flexDirection: "row" }}>
      {this.renderRightAction("trash", "#ef5350", 64, progress)}
    </View>
  );

  updateRef = ref => {
    this.swipeableRow = ref;
  };

  close = () => {
    this.swipeableRow.close();
  };

  render() {
    const {
      leftElement,
      title,
      description,
      rightElement,
      rightText,
      onPress,
      onLongPress,
      disabled
    } = this.props;

    const Component = onPress || onLongPress ? TouchableHighlight : View;

    const {
      itemContainer,
      leftElementContainer,
      rightSectionContainer,
      mainTitleContainer,
      rightElementContainer,
      rightTextContainer,
      titleStyle,
      descriptionStyle
    } = styles;

    return (
      <Swipeable
        ref={this.updateRef}
        friction={1}
        renderRightActions={this.renderRightActions}
      >
        <Component
          onPress={onPress}
          onLongPress={onLongPress}
          disabled={disabled}
          underlayColor="#f2f3f5"
        >
          <View style={itemContainer}>
            {leftElement ? (
              <View style={leftElementContainer}>{leftElement}</View>
            ) : (
              <View />
            )}
            <View style={rightSectionContainer}>
              <View style={mainTitleContainer}>
                <Text style={titleStyle}>{title}</Text>
                {description ? (
                  <Text style={descriptionStyle}>{description}</Text>
                ) : (
                  <View />
                )}
              </View>
              <View style={rightTextContainer}>
                {rightText ? <Text>{rightText}</Text> : <View />}
              </View>

              {rightElement ? (
                <View style={rightElementContainer}>{rightElement}</View>
              ) : (
                <View />
              )}
            </View>
          </View>
        </Component>
      </Swipeable>
    );
  }
}

const styles = StyleSheet.create({
  itemContainer: {
    flexDirection: "row",
    minHeight: 44,
    height: 63
  },
  leftElementContainer: {
    justifyContent: "center",
    alignItems: "center",
    flex: 2,
    paddingLeft: 13
  },
  rightSectionContainer: {
    marginLeft: 18,
    flexDirection: "row",
    flex: 20,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderColor: "#515151"
  },
  mainTitleContainer: {
    justifyContent: "center",
    flexDirection: "column",
    flex: 1
  },
  rightElementContainer: {
    justifyContent: "center",
    alignItems: "center",
    flex: 0.4
  },
  rightTextContainer: {
    justifyContent: "center",
    marginRight: 10
  },
  titleStyle: {
    fontSize: 16
  },
  descriptionStyle: {
    fontSize: 14,
    color: "#515151"
  },
  rightAction: {
    alignItems: "center",
    flex: 1,
    justifyContent: "center"
  }
});

export default ListItem;
