pipeline {
    agent { 
	label 'slave2'
    }
    environment {
        // Load properties from the file
        PROPS = readProperties file: 'pipeline-params.properties'
    }

    stages {
        stage('Checkout') {
            steps {
                // Use the parameters from the properties file
                git branch: "${env.PROPS.BRANCH_NAME}", url: 'https://github.com/your-repo.git'
            }
        }

        stage('Build') {
            steps {
                echo "Building branch ${env.PROPS.BRANCH_NAME}..."
                // Add your build steps here
            }
        }

        stage('Test') {
            when {
                expression { return env.PROPS.RUN_TESTS.toBoolean() }
            }
            steps {
                echo 'Running unit tests...'
                // Add your test steps here
            }
        }

        stage('Deploy') {
            steps {
                echo "Deploying to ${env.PROPS.ENVIRONMENT} environment..."
                // Add your deploy steps here
            }
        }
    }
}
