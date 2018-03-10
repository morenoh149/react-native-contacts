import React from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';

export default class Button extends React.Component {

  render () {
    return (
      <TouchableOpacity style={styles.container} onPress={this.props.onPress}>
        <Text style={styles.text}>{this.props.text}</Text>
      </TouchableOpacity>
    )
  }
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#444',
    padding: 20,
    alignItems: 'center',
    flex: 1,
    height: 50,
    margin: 5,
  },
  text: {
    textAlign: 'center',
    color: 'white',
  },
})
