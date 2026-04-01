Markdown
# 🚀 EKS CI/CD Pipeline with GitHub Actions, ECR, EKS, Blue-Green & Canary

## 📖 Overview

This project demonstrates a complete, end-to-end CI/CD pipeline that automates the building, storing, and deploying of a containerized Java application into Amazon EKS. 

The setup follows modern DevOps principles:
* **Infrastructure as Code:** Terraform (pre-provisioned platform).
* **Secure Authentication:** OIDC integration (no static AWS credentials).
* **Containerization:** Docker.
* **Orchestration:** Kubernetes orchestration using Amazon EKS.
* **Traffic Management:** Exposed externally via **AWS Application Load Balancer (ALB)** and Kubernetes **Ingress**.
* **Advanced Deployments:** Blue-Green & Canary strategies.
* **Observability:** Prometheus & Grafana.

The pipeline ensures that every code change is automatically built, pushed, and deployed without manual intervention while maintaining reliability and observability. 

> 👉 **The key idea is simple:** Every code change automatically flows from `GitHub → Build → ECR → EKS → User` without manual steps.

---

## 🏗️ Architecture Flow

```text
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
AWS Application Load Balancer (via Ingress)
 ↓
End User (Browser Access)
This architecture represents a secure and automated delivery pipeline. Instead of embedding AWS credentials, GitHub uses OIDC to assume an IAM role dynamically. The application is containerized and stored in ECR, while EKS handles orchestration, scaling, and self-healing. The Application Load Balancer (ALB) and Ingress expose the application externally, making the system production-ready.

Architecture Overview: This diagram visualizes the complete high-level architecture and data flow of the project. It illustrates how code pushed to GitHub triggers an OIDC-authenticated GitHub Action, which builds and pushes a Docker image to ECR. Finally, the image is deployed to an EKS cluster where traffic is managed via an Application Load Balancer, with Prometheus and Grafana handling the observability.

🔁 Data Flow (End-to-End System Movement)
This flow represents how code transforms into a running application. Each stage passes an artifact or signal to the next stage, forming a fully automated delivery chain:

GitHub → CI Pipeline → Docker Image → ECR → EKS → Pods → Service → ALB (Ingress) → User

🔄 Pipeline Execution Flow
Code Trigger
Any push to the master branch triggers the GitHub Actions workflow automatically. This ensures continuous integration and continuous deployment without manual intervention.

Continuous Integration: Here you can see the history of GitHub Actions workflow runs triggered by pushes to the master branch. It provides a clear audit trail of successful and failed deployments. This continuous integration setup ensures that every commit is automatically tested and processed without manual intervention.

Authentication (OIDC-Based Access)
The pipeline uses OIDC (OpenID Connect) instead of static credentials:

GitHub generates a short-lived identity token.

AWS validates it via the IAM OIDC provider.

The IAM role github-actions-eks-role is assumed.

Benefits:

No hardcoded secrets stored in GitHub.

Temporary credentials.

Strong security posture / secure access model.

Build Phase (Docker Image Creation)
The pipeline builds the Docker image:

Java application is compiled/packaged using Maven.

Dependencies are resolved.

Docker image is created.

Image is tagged using commit SHA to ensure immutability and traceability.

Bash
docker build -t dev-java-app:<commit-id> .
Automated Build Steps: This detailed view of a successful GitHub Actions job outlines the specific steps executed during the pipeline. It highlights the secure OIDC AWS authentication, logging into ECR, and the subsequent building and pushing of the Docker image. Each step is fully automated to ensure a consistent, error-free build process.

Push Phase (ECR Integration)
The Docker image is pushed to Amazon ECR:

Bash
docker push [816069164153.dkr.ecr.us-east-1.amazonaws.com/dev-java-app](https://816069164153.dkr.ecr.us-east-1.amazonaws.com/dev-java-app):<tag>
ECR acts as:

Private container registry.

Central artifact storage / Source of truth.

Source for Kubernetes deployments.

Each image version enables rollback, version control, and multi-version deployment strategies.

Private Image Registry: This shows the Amazon Elastic Container Registry (ECR) where our private repositories are hosted. ECR acts as the secure, central storage for our containerized applications before they are pulled by Kubernetes. It ensures our deployment artifacts are highly available and securely encrypted.

Image Versioning: Inside the ECR repository, you can see the individually tagged Docker images. Each image is tagged using its corresponding GitHub commit SHA to ensure complete immutability and traceability. This practice makes it incredibly simple to track which code version is running or to execute a rapid rollback if necessary.

Cluster Access (EKS Connection)
Bash
aws eks update-kubeconfig --region us-east-1 --name EKS-DEV
This step configures kubectl so the pipeline can interact directly with the EKS cluster.

Deployment Phase (Kubernetes Apply)
Bash
kubectl apply -f k8s/
This creates:

Deployment → manages pods lifecycle.

Service → exposes application internally/externally.

Rollout → handles canary strategy.

Kubernetes ensures desired state management, self-healing, and horizontal scaling.

Runtime Execution (EKS)
Initially, the deployment failed with ImagePullBackOff, a common real-world issue caused by incorrect image references or missing IAM permissions. After fixing image tag issues and IAM role permissions, Pods successfully transitioned to the Running state, confirming a stable deployment.

Key concept: Docker builds the image → Kubernetes runs the container.

Real-World Troubleshooting: During real-world deployments, issues like ImagePullBackOff or ErrImagePull can occur, as shown here. This usually indicates a typo in the image tag or missing IAM permissions for the EKS nodes to pull from ECR. Identifying and resolving these directly using kubectl is a core part of managing Kubernetes.

External Exposure (NodePort → ALB)
The application was first exposed using NodePort for validation and debugging.

Then exposed via AWS Application Load Balancer (ALB) using Ingress. This provides:

Public DNS endpoint.

Traffic distribution.

High availability.

External Application Access: Before setting up the Application Load Balancer, the application was exposed and tested using a Kubernetes NodePort. By curling the external IP of the EKS nodes on the designated port, we verified that the application was actively receiving and responding to traffic. This is a crucial validation step before routing production traffic.

🚦 Deployment Strategies
🔵🟢 Blue-Green Deployment
Two environments are maintained:

Blue → current stable version.

Green → new version.

Traffic switches safely between versions only after validation, ensuring zero downtime and instant rollback capability.

Successful Orchestration: This screenshot demonstrates a healthy Kubernetes cluster state after successfully applying our deployment manifests. You can clearly see the pods for both the blue and green deployments running concurrently in a healthy state. This validates that the orchestration logic is correctly managing the application's dual-environment lifecycle.

🟡 Canary Deployment
Traffic is gradually shifted to the new version while monitoring system behavior:

Small % of users → new version.

Metrics monitored.

Traffic increased step-by-step.
This reduces deployment risk and improves reliability.

🚀 Argo Rollouts (Progressive Delivery)
Argo Rollouts enhances Kubernetes deployments by providing fine-grained deployment control:

Traffic shifting in controlled stages: 20% → 50% → 100%.

Pause between stages.

Automated progressive delivery.

📊 Monitoring (Prometheus + Grafana)
Monitoring stack ensures observability, performance tracking, and early issue detection.

Prometheus → collects metrics.

Grafana → visualizes data.

Metrics collected and visualized for system health include CPU usage, memory usage, Pod health, and overall cluster performance.

Cluster Observability: This Grafana dashboard provides a comprehensive overview of the Kubernetes cluster's compute resources. It visualizes critical metrics scraped by Prometheus, such as overall CPU utilization, memory usage, and namespace resource limits. Having this observability is essential for capacity planning and detecting anomalies in real-time.

Workload Performance Comparison: This specific Grafana panel tracks the CPU usage of our isolated blue and green deployment workloads. Monitoring these side-by-side allows us to compare the performance and resource consumption of the new version against the stable version. It empowers the team to make data-driven decisions before fully cutting over user traffic.

🧠 Concepts Demonstrated
CI/CD automation using GitHub Actions

Secure AWS authentication using OIDC

Docker image lifecycle management

Kubernetes deployments, services, and ingress

Blue-Green and Canary deployment strategies

Progressive delivery using Argo Rollouts

Monitoring with Prometheus & Grafana

Real-world debugging (ImagePullBackOff, pipeline failures)

👩‍💻 Author
Asha 
— DevOps & AI-DevOps Engineer

⭐ Summary
This project showcases a real-world, production-grade DevOps pipeline where CI/CD, security, containerization, Kubernetes, deployment strategies, and monitoring work together to deliver a fully automated and reliable application deployment system.
