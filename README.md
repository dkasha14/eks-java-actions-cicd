# 🚀 EKS CI/CD Pipeline with GitHub Actions, ECR, EKS, Blue-Green & Canary

## 📖 Overview

This project demonstrates a complete, end-to-end CI/CD pipeline that automates the building, storing, and deploying of a containerized Java application into Amazon EKS.

The setup follows modern DevOps principles:
* **Infrastructure as Code:** Terraform (pre-provisioned platform).
* **Secure Authentication:** OIDC integration (no static AWS credentials).
* **Containerization:** Docker.
* **Orchestration:** Kubernetes orchestration using Amazon EKS.
* **Traffic Management:** Exposed externally via AWS Application Load Balancer (ALB) and Kubernetes Ingress.
* **Advanced Deployments:** Blue-Green & Canary strategies.
* **Observability:** Prometheus & Grafana.

The pipeline ensures that every code change is automatically built, pushed, and deployed without manual intervention while maintaining reliability and observability.

> 👉 **The key idea is simple:** Every code change automatically flows from GitHub → Build → ECR → EKS → User without manual steps.

---

## 🏗️ Architecture Flow
![Architecture](docs/images/01-cicd-terraform-eks-architecture.png)

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

Architecture Overview: This diagram visualizes the complete high-level architecture and data flow of the project. It illustrates how code pushed to GitHub triggers an OIDC-authenticated GitHub Action, which builds and pushes a Docker image to ECR. Finally, the image is deployed to an EKS cluster where traffic is managed via an Application Load Balancer, with Prometheus and Grafana handling observability.

🔁 Data Flow (End-to-End System Movement)
GitHub → CI Pipeline → Docker Image → ECR → EKS → Pods → Service → ALB (Ingress) → User

This flow represents how code transforms into a running application. Each stage passes an artifact or signal to the next stage, forming a fully automated delivery chain.

🔄 Pipeline Execution Flow
Code Trigger

![Architecture](docs/images/01-cicd-terraform-eks-architecture.png)

Any push to the master branch triggers the GitHub Actions workflow automatically. This ensures continuous integration and continuous deployment without manual intervention.

Continuous Integration: This screenshot shows the history of GitHub Actions runs, including both failures and successful executions. It acts as an audit trail and proves that the pipeline is fully automated and continuously improving through debugging and fixes.

Authentication (OIDC-Based Access)
The pipeline uses OIDC (OpenID Connect) instead of static credentials:

GitHub generates a short-lived identity token.

AWS validates it via the IAM OIDC provider.

The IAM role github-actions-eks-role is assumed.

Benefits: No hardcoded secrets stored in GitHub. Temporary credentials. Strong security posture.

Build Phase (Docker Image Creation)
The pipeline builds the Docker image:

Java application is compiled/packaged using Maven.

Dependencies are resolved.

Docker image is created.

Image is tagged using commit SHA to ensure immutability and traceability.

Bash
docker build -t dev-java-app:<commit-id> .
Automated Build Steps: This screenshot shows each stage of the GitHub Actions pipeline including AWS authentication, Docker build, and image push. Every step is automated, ensuring consistent builds across environments.

Push Phase (ECR Integration)
The Docker image is pushed to Amazon ECR. ECR acts as the private container registry, central artifact storage, and source for Kubernetes deployments.

Bash
docker push [816069164153.dkr.ecr.us-east-1.amazonaws.com/dev-java-app](https://816069164153.dkr.ecr.us-east-1.amazonaws.com/dev-java-app):<tag>
Private Image Registry: This screenshot shows the ECR repository storing Docker images securely.

Image Versioning: Each image is tagged with commit SHA, enabling rollback and traceability.

Cluster Access & Deployment (EKS)
Configure kubectl so the pipeline can interact directly with the EKS cluster, then apply the manifests:

Bash
aws eks update-kubeconfig --region us-east-1 --name EKS-DEV
kubectl apply -f k8s/
This creates the Deployment (manages pod lifecycle), Service (exposes application), and Rollout (handles canary strategy). Kubernetes ensures self-healing, scaling, and desired state.

Runtime Execution (EKS)
Initially, the deployment failed with ImagePullBackOff due to incorrect image references or IAM permission issues. After fixing, pods transitioned to Running state.

Real-World Troubleshooting: This demonstrates practical debugging using kubectl, which is critical in production systems.

External Exposure (NodePort → ALB)
The application was first exposed using NodePort as an initial validation step. In this approach, Kubernetes opens a specific port on every worker node, allowing external traffic to reach the application using the node’s public IP and port.

Once validated, the exposure strategy was upgraded to use an AWS Application Load Balancer (ALB) through Kubernetes Ingress.

External Access Validation: NodePort ensured that the application and service routing were functioning correctly at a basic level.

ALB Final Access: Moving to ALB introduced a production-grade traffic management layer, enabling scalability, reliability, and secure external access.

🚦 Deployment Strategies
🔵🟢 Blue-Green Deployment
Two environments are maintained simultaneously: Blue (stable) and Green (newly deployed). Once validation is complete, traffic is switched.

Successful Orchestration: This validates that the orchestration logic is correctly managing the application's dual-environment lifecycle.

🟡 Canary Deployment & Argo Rollouts
Canary deployment introduces the new version gradually. Argo Rollouts extends Kubernetes capabilities by enabling progressive delivery with fine-grained traffic control (e.g., 20% → 50% → 100%).

Progressive Delivery: Managing the rollout stages and traffic shifting via Argo Rollouts.

📊 Monitoring (Prometheus + Grafana)
Monitoring ensures that the system is performing optimally. Prometheus collects metrics, and Grafana visualizes them.

Cluster Observability: Grafana dashboards provide real-time visibility into the cluster’s health and performance.

Workload Comparison: Grafana dashboards showing resource usage and traffic patterns for both Blue and Green deployments.

Canary Monitoring: Dashboard tracking the resource health and traffic distribution during the Canary rollout.

---

# 🏗️ 1. High-Level Architecture

![Architecture](docs/images/01-cicd-terraform-eks-architecture.png)

This architecture represents a complete CI/CD ecosystem where code changes flow seamlessly from GitHub to a running application inside EKS. GitHub Actions acts as the automation engine, securely authenticating with AWS using OIDC, eliminating the need for static credentials. The Docker image is built and pushed to Amazon ECR, which acts as the container registry. Kubernetes (EKS) consumes these images and deploys them using different strategies like Blue-Green and Canary. Monitoring is tightly integrated using Prometheus and Grafana, enabling real-time observability of deployments and traffic behavior.

---

# 🔄 2. CI/CD Pipeline Execution History

![Pipeline History](docs/images/02-cicd-pipeline-history.png)

This screenshot shows the real pipeline execution history, including both successful and failed runs. It reflects the iterative debugging and stabilization process, which is a critical part of any real-world DevOps workflow. Failures here were primarily due to issues like incorrect IAM permissions and image pull errors, which were later resolved. The successful runs demonstrate a fully functional pipeline that builds, tags, and pushes Docker images reliably. This history proves the robustness and maturity of the pipeline rather than just a one-time success.

---

# ⚙️ 3. GitHub Actions Workflow Stages

![Workflow](docs/images/03-cicd-workflow-stages.png)

The pipeline is composed of multiple well-defined stages starting from code checkout to final deployment. It uses OIDC-based authentication to securely access AWS resources without storing secrets. The workflow builds a Docker image using Maven artifacts, tags it using the commit SHA, and pushes it to ECR. Each stage is modular and traceable, allowing easy debugging and observability. This reflects a production-grade CI/CD pipeline design where every step is automated and auditable.

---

# 📦 4. Amazon ECR Repository

![ECR Repo](docs/images/04-ecr-repository.png)

Amazon ECR acts as the centralized container registry where all Docker images are stored. This repository is configured to hold versioned images of the Java application, enabling traceability across deployments. Each image corresponds to a specific commit, ensuring rollback capability if needed. Using ECR ensures tight integration with EKS and IAM, making authentication and image pulling seamless. This is a critical component in decoupling build and deployment stages.

---

# 🐳 5. Docker Images in ECR

![ECR Images](docs/images/05-ecr-images.png)

This view shows multiple image versions pushed via the CI/CD pipeline. Each image tag corresponds to a Git commit SHA, ensuring immutability and traceability. Kubernetes deployments reference these tags, making deployments deterministic and reproducible. This also enables advanced strategies like Canary and Blue-Green deployments by switching between image versions. Maintaining multiple versions in ECR is essential for rollback and debugging production issues.

---

# ⚠️ 6. Kubernetes Image Pull Error (Debugging Phase)

![Error](docs/images/06-k8s-imagepull-error.png)

This stage captures a real-world failure scenario where Kubernetes pods failed due to `ImagePullBackOff`. This typically happens due to incorrect image URI, missing IAM permissions, or authentication issues with ECR. Debugging this required validating IAM roles, node permissions, and image existence in ECR. Fixing this issue ensured that worker nodes could securely pull images. This demonstrates hands-on troubleshooting experience, which is highly valued in DevOps roles.

---

# ✅ 7. Green Deployment Success

![Green](docs/images/07-k8s-green-deployment-success.png)

After resolving image pull issues, the Green deployment was successfully rolled out. This represents the new version of the application being deployed alongside the existing Blue version. Kubernetes ensures that the new pods are healthy before routing traffic. This approach minimizes downtime and risk during deployments. It forms the foundation for Blue-Green deployment strategy where traffic can be switched safely.

---

# 🌐 8. Application Access via NodePort

![NodePort](docs/images/08-nodeport-app-access.png)

The application is exposed using a NodePort service, allowing external access via node IP and port. This is typically used for testing and validation before introducing a load balancer. It verifies that the application is correctly deployed and reachable. While not production-grade exposure, it serves as an important intermediate validation step. This confirms that the container, service, and networking are functioning correctly.

---

# 📊 9. Cluster Monitoring (Grafana)

![Cluster](docs/images/09-grafana-cluster-monitoring.png)

Grafana dashboards provide cluster-level observability including CPU and memory usage across namespaces. This helps in understanding resource utilization and detecting anomalies. Monitoring is essential for validating deployment impact and ensuring system stability. It integrates with Prometheus which scrapes metrics from Kubernetes. This setup enables proactive alerting and performance tuning.

---

# 🔵🟢 10. Blue-Green Traffic Distribution

![BlueGreen](docs/images/10-grafana-blue-green-traffic.png)

This visualization shows traffic distribution between Blue (stable) and Green (new) deployments. Initially, traffic is gradually shifted to Green to validate its stability. This reduces the blast radius of potential failures. If issues are detected, traffic can be quickly routed back to Blue. This strategy ensures zero downtime and safe production releases.

---

# 🟡 11. Canary Deployment Traffic

![Canary](docs/images/11-grafana-blue-green-canary.png)

Canary deployment introduces a small percentage of traffic to a new version before full rollout. This allows real-time validation under actual user load. Metrics like CPU spikes or error rates are monitored during this phase. If everything looks stable, traffic is gradually increased. This is one of the safest deployment strategies used in production environments.

---

# 🚀 12. Argo Rollout (Canary Strategy)

![Rollout](docs/images/12-argo-rollout-canary.png)

Argo Rollouts enable advanced deployment strategies beyond standard Kubernetes deployments. In this setup, traffic is incrementally shifted (20%, 50%, 100%) with pause intervals. This allows monitoring between each step before proceeding. It provides automated control over progressive delivery. This is a powerful approach for minimizing deployment risk in large-scale systems.

---

# 🌍 13. Final Application via Load Balancer

![ALB](docs/images/13-alb-final-app-access.png)

Finally, the application is exposed using an AWS Load Balancer, making it accessible via a stable DNS endpoint. This represents a production-ready exposure mechanism. The load balancer distributes traffic across pods and ensures high availability. Combined with deployment strategies, it guarantees zero downtime releases. This completes the full CI/CD lifecycle from code to production.

---


🧠 Concepts Demonstrated
CI/CD automation using GitHub Actions.

Secure AWS authentication using OIDC.

Docker image lifecycle management.

Kubernetes deployments, services, ingress.

Blue-Green and Canary strategies (Argo Rollouts).

Monitoring with Prometheus & Grafana.

⭐ Summary
This project showcases a real-world, production-grade DevOps pipeline where CI/CD, security, containerization, Kubernetes, deployment strategies, and monitoring work together to deliver a fully automated and reliable application deployment system.
