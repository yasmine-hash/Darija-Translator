<?php
/**
 * Darija Translator - PHP Client
 * Usage: php translator.php "Hello, how are you?"
 * Or run without arguments for interactive mode
 */

// Configuration
define('API_URL', 'http://localhost:9090/api/translate');
define('USERNAME', 'user');
define('PASSWORD', 'password');

function translateToDarija($text) {
    $auth = base64_encode(USERNAME . ':' . PASSWORD);
    
    $data = json_encode(['text' => $text]);
    
    $options = [
        'http' => [
            'header' => [
                'Content-Type: application/json',
                'Authorization: Basic ' . $auth
            ],
            'method' => 'POST',
            'content' => $data
        ]
    ];
    
    $context = stream_context_create($options);
    
    try {
        $response = file_get_contents(API_URL, false, $context);
        
        if ($response === false) {
            throw new Exception("Failed to connect to API server");
        }
        
        $result = json_decode($response, true);
        
        if (isset($result['translation'])) {
            return $result['translation'];
        } else {
            return "Error: Invalid response from server";
        }
        
    } catch (Exception $e) {
        return "Error: " . $e->getMessage();
    }
}

// Main execution
if (php_sapi_name() === 'cli') {
    // Command line mode
    if ($argc > 1) {
        $text = implode(' ', array_slice($argv, 1));
        echo "\n📝 English: " . $text . "\n";
        echo "🔄 Translating...\n";
        $translation = translateToDarija($text);
        echo "🌍 Darija: " . $translation . "\n\n";
    } else {
        // Interactive mode
        echo "\n=== Darija Translator (PHP Client) ===\n";
        echo "Type 'quit' to exit\n\n";
        
        while (true) {
            echo "Enter English text: ";
            $text = trim(fgets(STDIN));
            
            if ($text === 'quit' || $text === 'exit') {
                echo "Goodbye!\n";
                break;
            }
            
            if (empty($text)) {
                echo "Please enter some text.\n";
                continue;
            }
            
            $translation = translateToDarija($text);
            echo "🌍 Darija: " . $translation . "\n\n";
        }
    }
} else {
    // Web mode
    $text = $_POST['text'] ?? '';
    if ($text) {
        header('Content-Type: application/json');
        echo json_encode(['translation' => translateToDarija($text)]);
    } else {
        ?>
        <!DOCTYPE html>
        <html>
        <head>
            <title>Darija Translator - PHP Client</title>
            <style>
                body { font-family: Arial; max-width: 600px; margin: 50px auto; padding: 20px; }
                textarea { width: 100%; padding: 10px; margin: 10px 0; }
                button { padding: 10px 20px; background: #667eea; color: white; border: none; cursor: pointer; }
                .result { margin-top: 20px; padding: 10px; background: #f0f0f0; border-radius: 5px; }
            </style>
        </head>
        <body>
            <h2>🌍 Darija Translator (PHP)</h2>
            <form method="POST">
                <textarea name="text" rows="4" placeholder="Enter English text..."></textarea>
                <button type="submit">Translate</button>
            </form>
            <?php if ($text): ?>
            <div class="result">
                <strong>Translation:</strong><br>
                <?php echo htmlspecialchars($translation); ?>
            </div>
            <?php endif; ?>
        </body>
        </html>
        <?php
    }
}
?>