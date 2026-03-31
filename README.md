EKS CI/CD Pipeline with GitHub Actions, ECR, EKS, Blue-Green & Canary
📖 Overview

This project demonstrates a complete CI/CD pipeline that automates the process of building, storing, and deploying a containerized Java application into Amazon EKS.

The system follows modern DevOps principles:

Infrastructure as Code (Terraform – already provisioned)
Secure authentication using OIDC (no AWS keys)
Container-based deployment using Docker
Kubernetes orchestration using EKS
Advanced deployment strategies (Blue-Green & Canary)
Monitoring using Prometheus & Grafana

The pipeline ensures that every code change is automatically built, pushed, and deployed without manual intervention while maintaining reliability and observability.

🏗️ Architecture Flow

GitHub Repository (Application Code)
↓
GitHub Actions (CI/CD Pipeline Execution)
↓
OIDC Authentication
↓
AWS IAM Role Assumption
↓
Docker Build (Container Image Creation)
↓
Amazon ECR (Private Image Registry)
↓
Amazon EKS Cluster
↓
Kubernetes Pods (Running Containers)
↓
Kubernetes Service (NodePort / LoadBalancer)
↓
AWS Application Load Balancer
↓
End User (Browser Access)

This architecture shows how code moves from development to production in a fully automated and secure manner. GitHub triggers the pipeline, which builds and stores container images in ECR. Kubernetes then pulls and runs these images. Services and ALB expose the application externally. Monitoring ensures visibility into runtime behavior.

🔄 Pipeline Execution Flow

1. Code Trigger

Any push to the master branch automatically triggers the GitHub Actions workflow. This ensures continuous integration and eliminates manual deployment steps.

2. Authentication (OIDC-Based Access)

Instead of storing AWS credentials, the pipeline uses OIDC:

GitHub generates a temporary token
AWS validates the token via OIDC provider
IAM role github-actions-eks-role is assumed

This provides:

No hardcoded secrets
Temporary credentials
Secure access model
3. Build Phase (Docker Image Creation)

The pipeline builds the Docker image:

Java application is packaged using Maven
Dependencies are resolved
Docker image is created
Image is tagged using commit SHA
docker build -t dev-java-app:<commit-id> .

This ensures each build is uniquely identifiable and traceable.

4. Push Phase (ECR Integration)

The Docker image is pushed to Amazon ECR:

docker push 816069164153.dkr.ecr.us-east-1.amazonaws.com/dev-java-app:<tag>

ECR acts as:

Private container registry
Central image storage
Source for Kubernetes deployments

Each image version represents a specific commit, enabling rollback and version control.

5. Cluster Access (EKS Connection)
aws eks update-kubeconfig --region us-east-1 --name EKS-DEV

This allows GitHub Actions to execute kubectl commands directly on the cluster.

6. Deployment Phase (Kubernetes Apply)
kubectl apply -f k8s/

This creates:

Deployment → manages pods
Service → exposes application
Rollout → manages canary strategy

Kubernetes ensures desired state is maintained automatically.

7. Runtime Execution (EKS)

Initially, deployment failed with ImagePullBackOff. This was due to incorrect image reference or permission issues between EKS and ECR. After fixing IAM roles and image tags, Kubernetes successfully pulled the image.

Pods were created and entered Running state, confirming successful deployment.

Key idea:

Docker builds the image
Kubernetes runs the container
8. External Exposure (NodePort → ALB)

Initially exposed using NodePort for validation.

Later exposed using AWS Load Balancer:

ALB automatically created
Traffic routed to pods
Application accessible publicly
🔁 Data Flow (End-to-End System Movement)
GitHub → CI Pipeline → Docker Image → ECR → EKS → Pods → Service → ALB → User

The data flow represents how application code transforms into a running service. Source code is converted into a Docker image, stored in ECR, and then pulled by Kubernetes nodes. The service routes traffic internally, while ALB exposes it externally. Meanwhile, monitoring systems continuously collect metrics, ensuring visibility into system performance.

🔵🟢 Blue-Green Deployment

Blue-Green deployment maintains two environments:

Blue → current stable version
Green → new version

Traffic is switched from Blue to Green once validation is complete. This ensures zero downtime and provides instant rollback capability.

🟡 Canary Deployment

Canary deployment releases the new version gradually:

Small percentage of traffic is routed to new version
Metrics are monitored
Traffic is increased progressively

This reduces risk and ensures safe deployment in production.

🚀 Argo Rollouts (Progressive Delivery)

Argo Rollouts enhances Kubernetes deployments:

Traffic shifts in stages (20% → 50% → 100%)
Pause between stages for monitoring
Automated progressive delivery

This provides fine-grained control over deployment strategy.

📊 Monitoring (Prometheus + Grafana)

Monitoring provides visibility into:

CPU usage
Memory usage
Namespace activity

Prometheus collects metrics, and Grafana visualizes them. This ensures system stability and helps detect performance issues early.

🧠 Concepts Demonstrated
CI/CD automation using GitHub Actions
Secure AWS authentication with OIDC
Docker image lifecycle management
Kubernetes deployments and services
Blue-Green and Canary deployment strategies
Monitoring with Prometheus & Grafana
Real-world debugging (ImagePullBackOff, pipeline failures)
👩‍💻 Author

Asha
— DevOps & AI-DevOps Engineer

⭐ Summary

This project showcases a real-world DevOps pipeline where CI/CD, security, containerization, Kubernetes, and monitoring work together to deliver a fully automated and production-ready deployment system.
