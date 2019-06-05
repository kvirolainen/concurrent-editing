package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.EditImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getDocumentTest() throws Exception {
        String documentJson = this.mvc.perform(post("/documents").param("name", "Demo"))
                .andExpect(status().is2xxSuccessful())
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();

        JsonNode documentNode = objectMapper.readTree(documentJson);
        int documentId = documentNode.get("id").asInt();

        this.mvc.perform(
                put("/documents/" + documentId + "/changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                EditImpl.constructInsert(0, 0, "Hi, ")
                        )))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        this.mvc.perform(
                put("/documents/" + documentId + "/changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                EditImpl.constructInsert(1, "Hi, ".length(), "world!")
                        )))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        this.mvc.perform(get("/documents/" + documentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.name").value("Demo"))
                .andExpect(jsonPath("$.text").value("Hi, world!"))
                .andExpect(jsonPath("$.id").value(documentId))
                .andExpect(jsonPath("$.version").value(2))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getDocumentChangesTest() throws Exception {
        String documentJson = this.mvc.perform(post("/documents").param("name", "Demo"))
                .andExpect(status().is2xxSuccessful())
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();

        JsonNode documentNode = objectMapper.readTree(documentJson);
        int documentId = documentNode.get("id").asInt();

        this.mvc.perform(
                put("/documents/" + documentId + "/changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                EditImpl.constructInsert(0, 0, "Hi, ")
                        )))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        this.mvc.perform(
                put("/documents/" + documentId + "/changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                EditImpl.constructInsert(1, "Hi, ".length(), "world!")
                        )))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        this.mvc.perform(get("/documents/" + documentId + "/changes").param("fromVersion", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0].text").value("Hi, "))
                .andExpect(jsonPath("$[1].text").value("world!"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void createDocumentTest() throws Exception {
        this.mvc.perform(post("/documents").param("name", "New Document"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.name").value("New Document"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void createAndRenameDocumentTest() throws Exception {
        String documentJson = this.mvc.perform(post("/documents").param("name", "New Document"))
                .andExpect(status().is2xxSuccessful())
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();

        JsonNode documentNode = objectMapper.readTree(documentJson);
        int documentId = documentNode.get("id").asInt();

        this.mvc.perform(put("/documents/" + documentId + "/name").param("name", "Old Document"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
        this.mvc.perform(get("/documents/" + documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Old Document"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void createAndDeleteDocumentTest() throws Exception {
        String documentJson = this.mvc.perform(post("/documents").param("name", "New Document"))
                .andExpect(status().is2xxSuccessful())
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();
        JsonNode documentNode = objectMapper.readTree(documentJson);
        int documentId = documentNode.get("id").asInt();

        this.mvc.perform(delete("/documents/" + documentId))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
        this.mvc.perform(get("/documents/" + documentId))
                .andExpect(status().is4xxClientError())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void createAndEditDocumentTest1() throws Exception {
        String documentJson = this.mvc.perform(post("/documents").param("name", "New Document"))
                .andExpect(status().is2xxSuccessful())
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();
        JsonNode documentNode = objectMapper.readTree(documentJson);
        int documentId = documentNode.get("id").asInt();

        this.mvc.perform(
                put("/documents/" + documentId + "/changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                EditImpl.constructInsert(0, 0, "This is my document")
                        )))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        this.mvc.perform(get("/documents/" + documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Document"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.text").value("This is my document"))
                .andDo(MockMvcResultHandlers.print());
    }

    /*
     * Concurrent editing test
     * User 1: This is my document -> This is document -> This is our document
     * User 2: This is my document -> This is my documents
     * Merged result: This is our documents
     */
    @Test
    public void createAndEditDocumentTest2() throws Exception {
        String documentJson = this.mvc.perform(post("/documents").param("name", "New Document"))
                .andExpect(status().is2xxSuccessful())
                .andDo(MockMvcResultHandlers.print())
                .andReturn().getResponse().getContentAsString();

        JsonNode documentNode = objectMapper.readTree(documentJson);
        int documentId = documentNode.get("id").asInt();

        this.mvc.perform(
                put("/documents/" + documentId + "/changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                EditImpl.constructInsert(0, 0, "This is my document")
                        )))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        this.mvc.perform(
                put("/documents/" + documentId + "/changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                EditImpl.constructDelete(1, "This is ".length(), "my".length())
                        )))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        this.mvc.perform(
                put("/documents/" + documentId + "/changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                EditImpl.constructInsert(2, "This is ".length(), "our")
                        )))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        this.mvc.perform(
                put("/documents/" + documentId + "/changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                EditImpl.constructInsert(1, "This is my document".length(), "s")
                        )))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        this.mvc.perform(get("/documents/" + documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Document"))
                .andExpect(jsonPath("$.version").value(4))
                .andExpect(jsonPath("$.text").value("This is our documents"))
                .andDo(MockMvcResultHandlers.print());
    }

}