// ===================================
// API CLIENT - HYBRID VERSION
// ===================================

const API_CONFIG = {
    BASE_URL: 'http://18.139.137.191:8080',  // ✔️ URL backend EC2 thật của bạn
};

// ===================================
// Helper Functions
// ===================================

function getAuthToken() {
    try {
        const user = JSON.parse(localStorage.getItem('user') || sessionStorage.getItem('user') || 'null');
        return user?.token || user?.accessToken || null;
    } catch (e) {
        return null;
    }
}

function getHeaders(includeAuth = true, contentType = 'application/json') {
    const headers = {};
    if (contentType) {
        headers['Content-Type'] = contentType;
    }
    if (includeAuth) {
        const token = getAuthToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
    }
    return headers;
}

async function handleResponse(response) {
    if (!response.ok) {
        let errorMessage = `HTTP error! status: ${response.status}`;
        try {
            const error = await response.json();
            errorMessage = error.message || errorMessage;
        } catch (e) {
            errorMessage = response.statusText || errorMessage;
        }
        throw new Error(errorMessage);
    }

    try {
        const text = await response.text();
        return text ? JSON.parse(text) : {};
    } catch (e) {
        return {};
    }
}

async function apiRequest(url, options = {}) {
    try {
        const fullUrl = `${API_CONFIG.BASE_URL}${url}`;
        const response = await fetch(fullUrl, {
            ...options,
            headers: {
                ...getHeaders(options.includeAuth !== false, options.contentType),
                ...(options.headers || {})
            }
        });
        return await handleResponse(response);
    } catch (error) {
        console.error(`API Request Error [${url}]:`, error);
        throw error;
    }
}

// ===================================
// BOOKS API
// ===================================

const BooksAPI = {
    async getAll() {
        try {
            const response = await apiRequest('/book');
            return response.data || [];
        } catch (error) {
            console.error('BooksAPI.getAll error:', error);
            return [];
        }
    },

    async getById(bookId) {
        const response = await apiRequest(`/book/${bookId}`);
        return response.data;
    },

    async create(bookData, coverImage, bookFile) {
        const formData = new FormData();
        formData.append('data', new Blob([JSON.stringify(bookData)], { type: 'application/json' }));
        if (coverImage) formData.append('image', coverImage);
        if (bookFile) formData.append('file', bookFile);

        const response = await apiRequest('/book', {
            method: 'POST',
            contentType: null,
            body: formData
        });
        return response.data;
    },

    async update(bookId, bookData, coverImage, bookFile) {
        const formData = new FormData();
        formData.append('data', new Blob([JSON.stringify(bookData)], { type: 'application/json' }));
        if (coverImage) formData.append('image', coverImage);
        if (bookFile) formData.append('file', bookFile);

        const response = await apiRequest(`/book/${bookId}/update`, {
            method: 'PUT',
            contentType: null,
            body: formData
        });
        return response.data;
    },

    async delete(bookId) {
        return await apiRequest(`/book/${bookId}`, { method: 'DELETE' });
    },

    async changeStatus(bookId) {
        return await apiRequest(`/book/${bookId}/change-status`, { method: 'PUT' });
    },

    async getByCategory(categoryId) {
        const response = await apiRequest(`/book/category/${categoryId}/books`);
        return response.data || [];
    },

    async getByAuthor(authorId) {
        const response = await apiRequest(`/book/author/${authorId}/books`);
        return response.data || [];
    },

    async search(keyword) {
        const response = await apiRequest(`/book/search?keyword=${encodeURIComponent(keyword)}`);
        return response.data || [];
    },

    async getNewest() {
        const response = await apiRequest('/book/newest');
        return response.data || [];
    },

    async getMostFavorite() {
        const response = await apiRequest('/book/most-favorite');
        return response.data || [];
    },

    async getMyFavorites() {
        const response = await apiRequest('/book/my-favorites');
        return response.data || [];
    },

    async toggleFavorite(bookId) {
        return await apiRequest(`/book/${bookId}/favorite`, { method: 'PUT' });
    }
};

// ===================================
// AUTHORS API
// ===================================

const AuthorsAPI = {
    async getAll() {
        const response = await apiRequest('/author', { includeAuth: true });
        return response.data || [];
    },

    async getById(authorId) {
        const response = await apiRequest(`/author/${authorId}`, { includeAuth: true });
        return response.data;
    },

    async create(authorData, imageFile) {
        const formData = new FormData();
        formData.append('data', new Blob([JSON.stringify(authorData)], { type: 'application/json' }));
        if (imageFile) formData.append('image', imageFile);

        const response = await fetch(`${API_CONFIG.BASE_URL}/author`, {
            method: 'POST',
            headers: { ...getHeaders(true, null) },
            body: formData
        });

        const data = await response.json();
        return data.data;
    }
};

// ===================================
// CATEGORIES API
// ===================================

const CategoriesAPI = {
    async getAll() {
        const response = await apiRequest('/category', { includeAuth: true });
        return response.data || [];
    },

    async getById(categoryId) {
        const response = await apiRequest(`/category/${categoryId}`, { includeAuth: true });
        return response.data;
    },

    async create(categoryData) {
        const response = await apiRequest('/category', {
            method: 'POST',
            body: JSON.stringify(categoryData),
            includeAuth: true,
            headers: { 'Content-Type': 'application/json' }
        });
        return response.data || response;
    }
};

// ===================================
// GLOBAL EXPORT
// ===================================

window.BooksAPI = BooksAPI;
window.AuthorsAPI = AuthorsAPI;
window.CategoriesAPI = CategoriesAPI;

export { BooksAPI, AuthorsAPI, CategoriesAPI };
