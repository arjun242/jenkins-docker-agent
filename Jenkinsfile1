pipeline {
    agent { 
	label 'slave2'
    }
    stages {
        stage('Load Environment Variables') {
            steps {
                sh 'source pipeline-params.sh'
            }
        }

        stage('Checkout') {
            steps {
                git branch: "${env.BRANCH_NAME}", url: 'https://github.com/your-repo.git'
            }
        }

        stage('Build') {
            steps {
                echo "Building branch ${env.BRANCH_NAME}..."
            }
        }

        stage('Test') {
            when {
                expression { return env.RUN_TESTS.toBoolean() }
            }
            steps {
                echo 'Running unit tests...'
            }
        }

        stage('Deploy') {
            steps {
                echo "Deploying to ${env.ENVIRONMENT} environment..."
            }
        }
    }
}
