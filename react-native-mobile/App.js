import React, { useState } from 'react';
import {
  SafeAreaView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
  ActivityIndicator,
  ScrollView,
  Alert,
  KeyboardAvoidingView,
  Platform
} from 'react-native';
import { StatusBar } from 'expo-status-bar';

// Configuration - Change this to your server IP
const API_URL = 'http://192.168.1.100:8080/api/translate'; // ⚠️ UPDATE THIS IP
const USERNAME = 'user';
const PASSWORD = 'password';

export default function App() {
  const [text, setText] = useState('');
  const [translation, setTranslation] = useState('');
  const [loading, setLoading] = useState(false);

  const translateText = async () => {
    if (!text.trim()) {
      Alert.alert('Error', 'Please enter some text to translate');
      return;
    }

    setLoading(true);
    setTranslation('');

    try {
      const auth = btoa(`${USERNAME}:${PASSWORD}`);
      
      const response = await fetch(API_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Basic ${auth}`,
        },
        body: JSON.stringify({ text: text.trim() }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.translation || `HTTP ${response.status}`);
      }

      setTranslation(data.translation);
    } catch (error) {
      console.error('Translation error:', error);
      Alert.alert(
        'Translation Failed',
        'Could not connect to the server. Make sure the Java backend is running and the IP address is correct.'
      );
      setTranslation('Error: Unable to translate');
    } finally {
      setLoading(false);
    }
  };

  const clearText = () => {
    setText('');
    setTranslation('');
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar style="light" />
      <KeyboardAvoidingView 
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardView}
      >
        <ScrollView contentContainerStyle={styles.scrollView}>
          <View style={styles.header}>
            <Text style={styles.title}>🌍 Darija Translator</Text>
            <Text style={styles.subtitle}>English → Moroccan Arabic</Text>
          </View>

          <View style={styles.card}>
            <Text style={styles.label}>English Text:</Text>
            <TextInput
              style={styles.textInput}
              multiline
              numberOfLines={4}
              placeholder="Enter English text here..."
              placeholderTextColor="#999"
              value={text}
              onChangeText={setText}
              editable={!loading}
            />

            <TouchableOpacity
              style={[styles.button, loading && styles.buttonDisabled]}
              onPress={translateText}
              disabled={loading}
            >
              {loading ? (
                <ActivityIndicator color="#fff" />
              ) : (
                <Text style={styles.buttonText}>🔄 Translate</Text>
              )}
            </TouchableOpacity>

            {translation ? (
              <View style={styles.resultContainer}>
                <Text style={styles.resultLabel}>Darija Translation:</Text>
                <View style={styles.resultBox}>
                  <Text style={styles.resultText}>{translation}</Text>
                </View>
                <TouchableOpacity 
                  style={styles.clearButton}
                  onPress={clearText}
                >
                  <Text style={styles.clearButtonText}>Clear</Text>
                </TouchableOpacity>
              </View>
            ) : null}
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  },
  keyboardView: {
    flex: 1,
  },
  scrollView: {
    flexGrow: 1,
    padding: 20,
  },
  header: {
    alignItems: 'center',
    marginTop: 20,
    marginBottom: 30,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#e0e0e0',
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 20,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 5,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 10,
  },
  textInput: {
    borderWidth: 2,
    borderColor: '#e0e0e0',
    borderRadius: 12,
    padding: 12,
    fontSize: 16,
    minHeight: 100,
    textAlignVertical: 'top',
    color: '#333',
  },
  button: {
    backgroundColor: '#667eea',
    padding: 15,
    borderRadius: 12,
    alignItems: 'center',
    marginTop: 20,
  },
  buttonDisabled: {
    backgroundColor: '#a0a0a0',
  },
  buttonText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: '600',
  },
  resultContainer: {
    marginTop: 20,
  },
  resultLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 10,
  },
  resultBox: {
    backgroundColor: '#f8f9fa',
    padding: 15,
    borderRadius: 12,
    borderLeftWidth: 4,
    borderLeftColor: '#667eea',
  },
  resultText: {
    fontSize: 16,
    color: '#333',
    lineHeight: 24,
  },
  clearButton: {
    marginTop: 15,
    padding: 10,
    alignItems: 'center',
  },
  clearButtonText: {
    color: '#764ba2',
    fontSize: 14,
    fontWeight: '600',
  },
});