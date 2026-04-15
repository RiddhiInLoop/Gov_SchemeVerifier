// js/app.js - Frontend JavaScript Logic bridging with Backend API endpoints

// --- Config and Utils ---
// We assume the Java backend exposes REST endpoints (e.g. via Spark, Spring Boot, or Servlets) at /api/*
// For this academic project, we mock the fetch calls if the backend server isn't running, but write them 
// perfectly as if a real Java Servlet or Spring Boot endpoint receives the JSON/Form data.
const API_BASE_URL = "http://localhost:8080/api";

// Helper to get URL Query params
function getQueryParam(param) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(param);
}

// Utility to get current logged in user from session
function getCurrentUser() {
    const userJson = sessionStorage.getItem('currentUser');
    return userJson ? JSON.parse(userJson) : null;
}

// Utility to set current logged in user
function setCurrentUser(user) {
    sessionStorage.setItem('currentUser', JSON.stringify(user));
}

// --- Navigation State Management ---
function updateNavbar() {
    const user = getCurrentUser();
    const loginLink = document.getElementById('nav-login');
    const registerLink = document.getElementById('nav-register');
    const dashboardLink = document.getElementById('nav-dashboard');
    const adminLink = document.getElementById('nav-admin');
    const logoutLink = document.getElementById('nav-logout');

    if (user) {
        if (loginLink) loginLink.style.display = 'none';
        if (registerLink) registerLink.style.display = 'none';

        if (user.role === 'admin' && adminLink) {
            adminLink.style.display = 'block';
            if (dashboardLink) dashboardLink.style.display = 'none';
        } else if (dashboardLink) {
            dashboardLink.style.display = 'block';
            if (adminLink) adminLink.style.display = 'none';
        }

        if (logoutLink) {
            logoutLink.style.display = 'block';
            logoutLink.addEventListener('click', (e) => {
                e.preventDefault();
                sessionStorage.removeItem('currentUser');
                window.location.href = 'index.html';
            });
        }
    } else {
        if (loginLink) loginLink.style.display = 'block';
        if (registerLink) registerLink.style.display = 'block';
        if (dashboardLink) dashboardLink.style.display = 'none';
        if (adminLink) adminLink.style.display = 'none';
        if (logoutLink) logoutLink.style.display = 'none';
    }
}

// --- Page Workflows ---
document.addEventListener('DOMContentLoaded', () => {
    updateNavbar();

    // 1. Citizen Registration
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (!registerForm.checkValidity()) {
                e.stopPropagation();
                registerForm.classList.add('was-validated');
                return;
            }

            const data = {
                firstName: document.getElementById('regFirstName').value,
                middleName: document.getElementById('regMiddleName').value,
                lastName: document.getElementById('regLastName').value,
                age: parseInt(document.getElementById('regAge').value),
                gender: document.getElementById('regGender').value,
                category: document.getElementById('regCategory').value,
                citizenship: document.getElementById('regCitizenship').value,
                income: parseFloat(document.getElementById('regIncome').value),
                residenceType: document.getElementById('regResidenceType').value,
                area: document.getElementById('regArea').value,
                landmark: document.getElementById('regLandmark').value,
                pinCode: document.getElementById('regPinCode').value,
                email: document.getElementById('regEmail').value,
                password: document.getElementById('regPassword').value
            };

            console.log("SENDING TO BACKEND POST /api/citizens/register", data);

            try {
                const response = await fetch(`${API_BASE_URL}/citizens/register`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });

                const result = await response.json();
                if (result.success) {
                    alert("Registration successful! Please login.");
                    window.location.href = 'login.html';
                } else {
                    alert("Registration failed. Please check your data.");
                }
            } catch (err) {
                console.error("Backend connection error", err);
                alert("Failed to connect to the backend server.");
            }
        });
    }

    // 2. Citizen Login
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (!loginForm.checkValidity()) {
                e.stopPropagation();
                loginForm.classList.add('was-validated');
                return;
            }

            const emailEl = document.getElementById('loginEmail');
            const passwordEl = document.getElementById('loginPassword');
            const email = emailEl ? emailEl.value : '';
            const password = passwordEl ? passwordEl.value : '';

            console.log(`SENDING TO BACKEND POST /api/citizens/login`, { email, password });

            try {
                const response = await fetch(`${API_BASE_URL}/citizens/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });

                if (response.ok) {
                    const userResp = await response.json();
                    setCurrentUser(userResp);
                    window.location.href = 'dashboard.html';
                } else {
                    const loginErrorEl = document.getElementById('loginError');
                    if (loginErrorEl) loginErrorEl.classList.remove('d-none');
                    else alert('Invalid credentials');
                }
            } catch (err) {
                console.error("Backend connection error", err);
                alert("Failed to connect to the backend server.");
            }
        });
    }

    // 3. Admin Login
    const adminLoginForm = document.getElementById('adminLoginForm');
    if (adminLoginForm) {
        adminLoginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            try {
                const response = await fetch(`${API_BASE_URL}/admin/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password })
                });

                if (response.ok) {
                    const userResp = await response.json();
                    setCurrentUser(userResp);
                    window.location.href = 'admin_dashboard.html';
                } else {
                    document.getElementById('loginError').classList.remove('d-none');
                }
            } catch (err) {
                console.error("Backend connection error", err);
                alert("Failed to connect to the backend server.");
            }
        });
    }

    // Admin Logout
    const adminLogoutBtn = document.getElementById('adminLogoutBtn');
    if (adminLogoutBtn) {
        adminLogoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            sessionStorage.removeItem('currentUser');
            window.location.href = 'admin.html';
        });
    }

    // General Logout button (dashboard/logout area)
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            sessionStorage.removeItem('currentUser');
            window.location.href = 'index.html';
        });
    }

    // 4. Fetch and Display Schemes
    const schemesList = document.getElementById('schemesList');
    if (schemesList) {
        // Helper function to render schemes
        function renderSchemes(schemes) {
            schemesList.innerHTML = ''; // clear loading spinner

            if (schemes.length === 0) {
                schemesList.innerHTML = '<div class="col-12"><p class="text-center text-muted">No schemes available.</p></div>';
                return;
            }

            schemes.forEach(scheme => {
                const reqString = `Min Age: ${scheme.minAge} | Max Income: ₹${scheme.incomeLimit}`;
                const cardHTML = `
                    <div class="col-md-6 col-lg-4">
                        <div class="card scheme-card h-100">
                            <div class="card-header pb-1">
                                <h5 class="card-title text-primary">${scheme.name}</h5>
                            </div>
                            <div class="card-body d-flex flex-column">
                                <p class="card-text text-muted mb-3">${scheme.description}</p>
                                <div class="mt-auto mb-3">
                                    <small class="text-uppercase fw-bold text-muted d-block border-top pt-2">Requirements:</small>
                                    <span class="badge bg-light text-dark border">${reqString}</span>
                                </div>
                                <a href="eligibility.html?schemeId=${scheme.id}" class="btn btn-outline-primary w-100 fw-bold">Check Eligibility</a>
                            </div>
                        </div>
                    </div>
                `;
                schemesList.insertAdjacentHTML('beforeend', cardHTML);
            });
        }

        // === Scheme Fetching ===
        async function fetchSchemes() {
            try {
                const response = await fetch(`${API_BASE_URL}/schemes`);
                const dbSchemes = await response.json();

                // Cache schemes for later use (specifically for checking eligibility)
                window.schemesCache = dbSchemes;
                renderSchemes(dbSchemes);

                // Search bar filtering logic
                const searchInput = document.getElementById('schemeSearchInput');
                if (searchInput) {
                    searchInput.addEventListener('input', (e) => {
                        const searchTerm = e.target.value.toLowerCase();
                        const filteredSchemes = window.schemesCache.filter(scheme => 
                            scheme.name.toLowerCase().includes(searchTerm) ||
                            (scheme.description && scheme.description.toLowerCase().includes(searchTerm))
                        );
                        renderSchemes(filteredSchemes);
                    });
                }
            } catch (error) {
                console.error("Error fetching schemes:", error);
                schemesList.innerHTML = '<div class="col-12 text-danger text-center mt-4"><h4>Failed to load schemes from database.</h4><p>Make sure Java Backend is running on port 8080.</p></div>';
            }
        }
        fetchSchemes();
    }

    // 5. Check Eligibility Logic
    const eligibilityResultContainer = document.getElementById('eligibilityResultContainer');
    if (eligibilityResultContainer) {
        const schemeId = parseInt(getQueryParam('schemeId'));
        const user = getCurrentUser();

        if (isNaN(schemeId) || !user) {
            eligibilityResultContainer.innerHTML = `< h3 class="text-danger" > Error: Login required or Invalid Scheme</h3 >
                    <a href="login.html" class="btn btn-primary mt-3">Login to Continue</a>`;
            return;
        }

        // Actual Backend call for Eligibility Check
        async function checkEligibility() {
            try {
                const response = await fetch(`${API_BASE_URL}/eligibility/check?citizenId=${user.id}&schemeId=${schemeId}`);
                if (!response.ok) {
                    const text = await response.text();
                    console.error('Eligibility service returned error', response.status, text);
                    eligibilityResultContainer.innerHTML = `<h3 class="text-danger">Eligibility Service error ${response.status}: ${text}</h3>`;
                    return;
                }
                const data = await response.json();

                if (data.eligible) {
                    eligibilityResultContainer.innerHTML = `
                        <i class="bi bi-check-circle-fill text-success" style="font-size: 4rem;"></i>
                        <h2 class="text-success mt-3">You are Eligible!</h2>
                        <p class="text-muted">${data.message || 'You meet the eligibility criteria.'}</p>
                        <a href="apply.html?schemeId=${schemeId}" class="btn btn-success btn-lg mt-3 px-5">Proceed to Apply</a>
                    `;
                } else {
                    eligibilityResultContainer.innerHTML = `
                        <i class="bi bi-x-circle-fill text-danger" style="font-size: 4rem;"></i>
                        <h2 class="text-danger mt-3">Not Eligible</h2>
                        <p class="text-muted">${data.message || 'You do not meet the scheme criteria.'}</p>
                        <a href="dashboard.html" class="btn btn-outline-secondary mt-3 px-5">Back to Dashboard</a>
                    `;
                }
            } catch (error) {
                console.error("Eligibility Check Error:", error);
                eligibilityResultContainer.innerHTML = `<h3 class="text-danger">Failed to connect to Eligibility Service: ${error.message}</h3>`;
            }
        }

        checkEligibility();
    }

    // 6. Application Submit
    const applyForm = document.getElementById('applyForm');
    if (applyForm) {
        const schemeId = getQueryParam('schemeId');
        const user = getCurrentUser();

        if (!schemeId || !user) {
            const eligibilityResultContainer = document.getElementById('eligibilityResultContainer');
            if (eligibilityResultContainer) {
                eligibilityResultContainer.innerHTML = `<h3 class="text-danger">Error: Login required or Invalid Scheme</h3>
                    <a href="login.html" class="btn btn-primary mt-3">Login to Continue</a>`;
            }
            return;
        }

        applyForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            try {
                const response = await fetch(`${API_BASE_URL}/applications/apply`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ citizenId: user.id, schemeId: parseInt(schemeId) })
                });

                const result = await response.json();
                if (result.success) {
                    alert("Application submitted successfully!");
                    window.location.href = 'dashboard.html';
                } else {
                    alert(result.message || "Failed to submit application. Did you already apply?");
                }
            } catch (error) {
                console.error("Error applying for scheme:", error);
                alert("An error occurred. Make sure the Java Backend is running.");
            }
        });
    }

    // Citizen Dashboard Initialization
    const dashApplicationsBody = document.getElementById('dashApplicationsBody');
    if (dashApplicationsBody) {
        const user = getCurrentUser();
        if (!user) {
            window.location.href = 'login.html';
            return;
        }

        // 1. Populate User Info
        document.getElementById('dashUserName').innerText = user.name || user.firstName;

        const userInfoList = document.getElementById('dashUserInfo');
        if (userInfoList) {
            userInfoList.innerHTML = `
                <li class="list-group-item d-flex justify-content-between align-items-center">
                    <span class="text-muted"><i class="bi bi-person me-2"></i>Name</span>
                    <span class="fw-bold">${user.name || (user.firstName + ' ' + user.lastName)}</span>
                </li>
                <li class="list-group-item d-flex justify-content-between align-items-center">
                    <span class="text-muted"><i class="bi bi-envelope me-2"></i>Email</span>
                    <span>${user.email}</span>
                </li>
                <li class="list-group-item d-flex justify-content-between align-items-center">
                    <span class="text-muted"><i class="bi bi-calendar me-2"></i>Age</span>
                    <span>${user.age} Yrs</span>
                </li>
                <li class="list-group-item d-flex justify-content-between align-items-center">
                    <span class="text-muted"><i class="bi bi-cash me-2"></i>Income</span>
                    <span>₹${user.income}</span>
                </li>
            `;
        }

        // 2. Fetch User Applications
        async function fetchCitizenApplications() {
            try {
                const response = await fetch(`${API_BASE_URL}/applications/citizen/${user.id}`);
                const apps = await response.json();

                let html = '';
                if (apps.length === 0) {
                    html = '<tr><td colspan="5" class="text-center text-muted py-4">You have not applied for any schemes yet.</td></tr>';
                } else {
                    apps.forEach(app => {
                        let statusClass = "status-pending";
                        if (app.status === 'Approved') statusClass = "status-approved";
                        if (app.status === 'Rejected') statusClass = "status-rejected";

                        html += `
                        <tr>
                            <td class="text-muted ps-4">#${app.applicationId}</td>
                            <td class="fw-bold text-primary">${app.schemeName}</td>
                            <td>${app.appliedDate}</td>
                            <td><span class="status-badge ${statusClass}">${app.status}</span></td>
                            <td class="text-muted text-truncate" style="max-width: 150px;" title="${app.rejectionReason || ''}">${app.rejectionReason || '-'}</td>
                        </tr>
                        `;
                    });
                }
                dashApplicationsBody.innerHTML = html;
            } catch (error) {
                console.error("Error fetching citizen apps:", error);
                dashApplicationsBody.innerHTML = '<tr><td colspan="5" class="text-center text-danger py-4">Failed to load applications. Ensure backend is running.</td></tr>';
            }
        }

        fetchCitizenApplications();
    }

    // 7. Render Admin Dashboard
    const adminTableBody = document.getElementById('adminTableBody');
    if (adminTableBody) {

        async function fetchAdminApplications() {
            try {
                const response = await fetch(`${API_BASE_URL}/admin/applications`);
                const apps = await response.json();

                let html = '';
                if (apps.length === 0) {
                    html = '<tr><td colspan="6" class="text-center text-muted">No applications found.</td></tr>';
                } else {
                    apps.forEach(app => {
                        let actionDocs = '';
                        if (app.status === 'Pending') {
                            actionDocs = `
                            <button class="btn btn-sm btn-primary me-1 view-btn" data-id="${app.applicationId}">View</button>
                            <button class="btn btn-sm btn-success me-1 approve-btn" data-id="${app.applicationId}">Approve</button>
                            <button class="btn btn-sm btn-danger reject-btn" data-id="${app.applicationId}">Reject</button>
                            `;
                        } else {
                            actionDocs = `
                            <button class="btn btn-sm btn-primary me-1 view-btn" data-id="${app.applicationId}">View</button>
                            <span class="text-muted small">Reviewed</span>`;
                        }

                        let statusClass = "status-pending";
                        if (app.status === 'Approved') statusClass = "status-approved";
                        if (app.status === 'Rejected') statusClass = "status-rejected";

                        html += `
                        <tr>
                            <td class="text-muted">#${app.applicationId}</td>
                            <td class="fw-bold">${app.citizenName}</td>
                            <td class="text-primary">${app.schemeName}</td>
                            <td>${app.appliedDate}</td>
                            <td><span class="status-badge ${statusClass}">${app.status}</span></td>
                            <td class="text-end pe-4">${actionDocs}</td>
                        </tr>
                        `;
                    });
                }
                adminTableBody.innerHTML = html;

                // Attach event listeners for the new buttons
                document.querySelectorAll('.view-btn').forEach(btn => {
                    btn.addEventListener('click', (e) => {
                        const appId = e.target.dataset.id;
                        const appDetails = apps.find(a => a.applicationId == appId);
                        if (appDetails) {
                            const details = `
                                <h6>Application Summary</h6>
                                <hr>
                                <p><strong>App ID:</strong> #${appDetails.applicationId}</p>
                                <p><strong>Citizen Name:</strong> ${appDetails.citizenName}</p>
                                <p><strong>Scheme Requested:</strong> ${appDetails.schemeName}</p>
                                <div class="bg-primary text-white p-2 rounded mt-3">
                                    <strong>Date of Application:</strong> ${appDetails.appliedDate}
                                </div>
                                <p class="mt-3"><strong>Status:</strong> ${appDetails.status}</p>
                            `;
                            document.getElementById('modalAppDetails').innerHTML = details;
                            const viewModal = new bootstrap.Modal(document.getElementById('viewDetailsModal'));
                            viewModal.show();
                        }
                    });
                });

                document.querySelectorAll('.approve-btn').forEach(btn => {
                    btn.addEventListener('click', async (e) => {
                        const appId = e.target.dataset.id;
                        await processApplicationAction(appId, 'approve', null);
                    });
                });

                document.querySelectorAll('.reject-btn').forEach(btn => {
                    btn.addEventListener('click', async (e) => {
                        const appId = e.target.dataset.id;
                        const reason = prompt("Enter Rejection Reason:");
                        if (reason) {
                            await processApplicationAction(appId, 'reject', reason);
                        }
                    });
                });

            } catch (error) {
                console.error("Admin fetch apps error: ", error);
                adminTableBody.innerHTML = '<tr><td colspan="6" class="text-center text-danger">Failed to load applications. Ensure backend is running.</td></tr>';
            }
        }

        async function processApplicationAction(appId, action, reason) {
            try {
                const url = `${API_BASE_URL}/admin/${action}/${appId}`;
                const body = reason ? JSON.stringify({ reason }) : null;
                const opts = { method: 'POST', headers: { 'Content-Type': 'application/json' } };
                if (body) opts.body = body;

                const response = await fetch(url, opts);
                const result = await response.json();

                if (result.success) {
                    alert(`Application ${action}d successfully.`);
                    fetchAdminApplications(); // refresh table
                } else {
                    alert(`Failed to ${action} application.`);
                }
            } catch (error) {
                console.error("Error processing application action:", error);
                alert("An error occurred. Check backend connection.");
            }
        }

        // Initial fetch
        fetchAdminApplications();
    }

    // 8. Admin Scheme Management
    const adminSchemesTableBody = document.getElementById('adminSchemesTableBody');
    if (adminSchemesTableBody) {

        async function fetchAdminSchemes() {
            try {
                const response = await fetch(`${API_BASE_URL}/schemes`);
                const schemas = await response.json();

                // Cache them here too for easy editing
                window.adminSchemesCache = schemas;

                let html = '';
                if (schemas.length === 0) {
                    html = '<tr><td colspan="6" class="text-center text-muted py-4">No active schemes found.</td></tr>';
                } else {
                    schemas.forEach(scheme => {
                        html += `
                        <tr>
                            <td class="text-muted">#${scheme.id}</td>
                            <td class="fw-bold text-primary">${scheme.name}</td>
                            <td>${scheme.minAge} Yrs</td>
                            <td>${scheme.maxAge} Yrs</td>
                            <td>₹${scheme.incomeLimit}</td>
                            <td class="text-end pe-4">
                                <button class="btn btn-sm btn-outline-primary me-1 edit-scheme-btn" data-id="${scheme.id}" data-bs-toggle="modal" data-bs-target="#schemeModal">Edit</button>
                                <button class="btn btn-sm btn-outline-danger delete-scheme-btn" data-id="${scheme.id}">Delete</button>
                            </td>
                        </tr>
                        `;
                    });
                }
                adminSchemesTableBody.innerHTML = html;

                // Attach edit/delete listeners
                document.querySelectorAll('.edit-scheme-btn').forEach(btn => {
                    btn.addEventListener('click', (e) => {
                        const sId = parseInt(e.target.dataset.id);
                        const scheme = window.adminSchemesCache.find(s => s.id === sId);
                        if (scheme) populateSchemeModal(scheme);
                    });
                });

                document.querySelectorAll('.delete-scheme-btn').forEach(btn => {
                    btn.addEventListener('click', async (e) => {
                        if (confirm("Are you sure you want to delete this scheme? This will affect existing applications.")) {
                            await deleteScheme(e.target.dataset.id);
                        }
                    });
                });

            } catch (error) {
                console.error("Admin fetch schemes error:", error);
                adminSchemesTableBody.innerHTML = '<tr><td colspan="6" class="text-center text-danger py-4">Failed to load schemes. Ensure backend is running.</td></tr>';
            }
        }

        // Form Handling
        const schemeForm = document.getElementById('schemeForm');
        if (schemeForm) {
            schemeForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                await saveScheme();
            });

            // Clear form when "Add New" is clicked
            document.getElementById('addSchemeBtn')?.addEventListener('click', () => {
                schemeForm.reset();
                document.getElementById('schemeFormId').value = '';
                document.getElementById('schemeModalLabel').innerText = 'Add New Scheme';
            });
        }

        function populateSchemeModal(scheme) {
            document.getElementById('schemeModalLabel').innerText = 'Edit Scheme Data';
            document.getElementById('schemeFormId').value = scheme.id;
            document.getElementById('schemeFormName').value = scheme.name;
            document.getElementById('schemeFormDesc').value = scheme.description || '';
            document.getElementById('schemeFormMinAge').value = scheme.minAge;
            document.getElementById('schemeFormMaxAge').value = scheme.maxAge;
            document.getElementById('schemeFormIncome').value = scheme.incomeLimit;
            document.getElementById('schemeFormGenderReq').value = scheme.genderReq || 'All';
            document.getElementById('schemeFormCategoryReq').value = scheme.categoryReq || 'All';
            document.getElementById('schemeFormCitizenshipReq').value = scheme.citizenshipReq || 'Indian';
        }

        async function saveScheme() {
            const id = document.getElementById('schemeFormId').value;
            const payload = {
                name: document.getElementById('schemeFormName').value,
                description: document.getElementById('schemeFormDesc').value,
                minAge: parseInt(document.getElementById('schemeFormMinAge').value),
                maxAge: parseInt(document.getElementById('schemeFormMaxAge').value),
                incomeLimit: parseFloat(document.getElementById('schemeFormIncome').value),
                genderReq: document.getElementById('schemeFormGenderReq').value,
                categoryReq: document.getElementById('schemeFormCategoryReq').value,
                citizenshipReq: document.getElementById('schemeFormCitizenshipReq').value
            };

            let method = 'POST'; // Add
            if (id) {
                method = 'PUT'; // Edit
                payload.schemeId = parseInt(id);
            }

            try {
                const response = await fetch(`${API_BASE_URL}/admin/schemes`, {
                    method: method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                const result = await response.json();
                if (result.success) {
                    alert(id ? "Scheme updated successfully!" : "Scheme added successfully!");
                    // Close modal and refresh text
                    const modal = bootstrap.Modal.getInstance(document.getElementById('schemeModal'));
                    if (modal) modal.hide();
                    fetchAdminSchemes();
                } else {
                    alert("Failed to save scheme. Check inputs.");
                }
            } catch (err) {
                console.error("Save scheme error:", err);
                alert("Error communicating with Backend.");
            }
        }

        async function deleteScheme(id) {
            try {
                const response = await fetch(`${API_BASE_URL}/admin/schemes/${id}`, { method: 'DELETE' });
                const result = await response.json();
                if (result.success) {
                    alert("Scheme deleted successfully!");
                    fetchAdminSchemes();
                } else {
                    alert("Failed to delete scheme. It might have active applications attached.");
                }
            } catch (err) {
                console.error("Delete scheme error:", err);
                alert("Error communicating with Backend.");
            }
        }

        fetchAdminSchemes();
    }
});
