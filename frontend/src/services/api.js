import axios from 'axios';

const API_URL = 'http://localhost:8080/api/reviews';

export const uploadFile = async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await axios.post(`${API_URL}/upload`, formData);
    return response.data;
};

export const getAnalysis = async (batchId) => {
    const response = await axios.get(`${API_URL}/analysis/${batchId}`);
    return response.data;
};
