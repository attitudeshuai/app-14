const API_BASE = '';

let authToken = localStorage.getItem('authToken') || '';

function setAuthToken(token) {
    authToken = token;
    localStorage.setItem('authToken', token);
}

function clearAuthToken() {
    authToken = '';
    localStorage.removeItem('authToken');
}

function getHeaders() {
    const headers = {
        'Content-Type': 'application/json'
    };
    if (authToken) {
        headers['Authorization'] = 'Bearer ' + authToken;
    }
    return headers;
}

async function apiRequest(url, options = {}) {
    const response = await fetch(API_BASE + url, {
        ...options,
        headers: {
            ...getHeaders(),
            ...options.headers
        }
    });
    const data = await response.json();
    if (data.code !== 200) {
        throw new Error(data.message || '请求失败');
    }
    return data.data;
}

async function uploadImage(file) {
    const formData = new FormData();
    formData.append('file', file);
    
    const headers = {};
    if (authToken) {
        headers['Authorization'] = 'Bearer ' + authToken;
    }
    
    const response = await fetch(API_BASE + '/api/upload/image', {
        method: 'POST',
        headers: headers,
        body: formData
    });
    const data = await response.json();
    if (data.code !== 200) {
        throw new Error(data.message || '上传失败');
    }
    return data.data;
}

async function uploadImages(files) {
    const formData = new FormData();
    for (let file of files) {
        formData.append('files', file);
    }
    
    const headers = {};
    if (authToken) {
        headers['Authorization'] = 'Bearer ' + authToken;
    }
    
    const response = await fetch(API_BASE + '/api/upload/images', {
        method: 'POST',
        headers: headers,
        body: formData
    });
    const data = await response.json();
    if (data.code !== 200) {
        throw new Error(data.message || '上传失败');
    }
    return data.data;
}

async function login(username, password) {
    const data = await apiRequest('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password })
    });
    setAuthToken(data.token);
    return data;
}

async function createPet(petData) {
    return await apiRequest('/api/pets', {
        method: 'POST',
        body: JSON.stringify(petData)
    });
}

async function createPetWithPhoto(formData) {
    const headers = {};
    if (authToken) {
        headers['Authorization'] = 'Bearer ' + authToken;
    }
    
    const response = await fetch(API_BASE + '/api/pets', {
        method: 'POST',
        headers: headers,
        body: formData
    });
    const data = await response.json();
    if (data.code !== 200) {
        throw new Error(data.message || '创建失败');
    }
    return data.data;
}

async function createDailyLog(logData) {
    return await apiRequest('/api/fosterdailylogs', {
        method: 'POST',
        body: JSON.stringify(logData)
    });
}

async function createDailyLogWithPhotos(formData) {
    const headers = {};
    if (authToken) {
        headers['Authorization'] = 'Bearer ' + authToken;
    }
    
    const response = await fetch(API_BASE + '/api/fosterdailylogs', {
        method: 'POST',
        headers: headers,
        body: formData
    });
    const data = await response.json();
    if (data.code !== 200) {
        throw new Error(data.message || '创建失败');
    }
    return data.data;
}

async function getMyPets() {
    return await apiRequest('/api/pets/mine');
}

async function getPetById(id) {
    return await apiRequest('/api/pets/' + id);
}

async function getFosterRequests() {
    return await apiRequest('/api/fosterrequests/mine');
}

async function getFosterRequestById(id) {
    return await apiRequest('/api/fosterrequests/' + id);
}

async function createFosterRequest(requestData) {
    return await apiRequest('/api/fosterrequests', {
        method: 'POST',
        body: JSON.stringify(requestData)
    });
}

async function createReview(reviewData) {
    return await apiRequest('/api/fosterreviews', {
        method: 'POST',
        body: JSON.stringify(reviewData)
    });
}

async function getReviews(params = {}) {
    const query = new URLSearchParams(params).toString();
    return await apiRequest('/api/fosterreviews' + (query ? '?' + query : ''));
}

async function getReviewById(id) {
    return await apiRequest('/api/fosterreviews/' + id);
}

async function getMyFosterStats() {
    return await apiRequest('/api/stats/mine');
}

async function getReputation(userId) {
    return await apiRequest('/api/reputation/' + userId);
}

async function getMyReputation() {
    return await apiRequest('/api/reputation/mine');
}

function showMessage(message, type = 'success') {
    const msgDiv = document.createElement('div');
    msgDiv.className = `alert alert-${type}`;
    msgDiv.textContent = message;
    msgDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 9999;
        padding: 15px 25px;
        border-radius: 8px;
        color: white;
        font-weight: 500;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;
    msgDiv.style.backgroundColor = type === 'success' ? '#10b981' : '#ef4444';
    document.body.appendChild(msgDiv);
    
    setTimeout(() => {
        msgDiv.style.transition = 'opacity 0.3s';
        msgDiv.style.opacity = '0';
        setTimeout(() => msgDiv.remove(), 300);
    }, 3000);
}

function createImagePreview(file, onRemove) {
    const reader = new FileReader();
    const container = document.createElement('div');
    container.className = 'preview-item';
    container.style.cssText = `
        position: relative;
        width: 120px;
        height: 120px;
        border-radius: 8px;
        overflow: hidden;
        margin: 8px;
        display: inline-block;
        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    `;
    
    reader.onload = function(e) {
        const img = document.createElement('img');
        img.src = e.target.result;
        img.style.cssText = `
            width: 100%;
            height: 100%;
            object-fit: cover;
        `;
        container.appendChild(img);
        
        const removeBtn = document.createElement('button');
        removeBtn.innerHTML = '×';
        removeBtn.style.cssText = `
            position: absolute;
            top: 4px;
            right: 4px;
            width: 24px;
            height: 24px;
            border-radius: 50%;
            background: rgba(0,0,0,0.6);
            color: white;
            border: none;
            cursor: pointer;
            font-size: 16px;
            line-height: 24px;
            text-align: center;
            padding: 0;
        `;
        removeBtn.onclick = function() {
            container.remove();
            if (onRemove) onRemove();
        };
        container.appendChild(removeBtn);
    };
    reader.readAsDataURL(file);
    
    return container;
}
