
import axios from 'axios'

const instance = axios.create({
    baseURL: 'http://' + '192.168.1.9:8080' + '/2020WAR/'
});

export default instance;