package com.reliaquest.api.client;

import com.reliaquest.api.model.Employee;
import java.util.List;

public final class EmployeeApiResponses {
    private EmployeeApiResponses() {}
    public static class EmployeeListResponse {
        private List<Employee> data;
        private String status;

        public List<Employee> getData() {
            return data;
        }

        public void setData(List<Employee> data) {
            this.data = data;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class EmployeeSingleResponse {
        private Employee data;
        private String status;

        public Employee getData() {
            return data;
        }

        public void setData(Employee data) {
            this.data = data;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class DeleteResponse {
        private Boolean data;
        private String status;

        public Boolean getData() {
            return data;
        }

        public void setData(Boolean data) {
            this.data = data;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
