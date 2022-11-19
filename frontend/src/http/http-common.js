import axios from "axios";

export const HTTP = axios.create({
        baseURL: 'http://localhost:8070/api/',
        withCredentials: false,
    }
)