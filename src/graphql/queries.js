/* eslint-disable */
// this is an auto generated file. This will be overwritten

export const getTodoApiKey = /* GraphQL */ `
  query GetTodoApiKey($id: ID!) {
    getTodoApiKey(id: $id) {
      name
      description
      id
      createdAt
      updatedAt
    }
  }
`;
export const listTodoApiKeys = /* GraphQL */ `
  query ListTodoApiKeys(
    $filter: ModelTodoApiKeyFilterInput
    $limit: Int
    $nextToken: String
  ) {
    listTodoApiKeys(filter: $filter, limit: $limit, nextToken: $nextToken) {
      items {
        name
        description
        id
        createdAt
        updatedAt
      }
      nextToken
    }
  }
`;
export const getTodoIam = /* GraphQL */ `
  query GetTodoIam($id: ID!) {
    getTodoIam(id: $id) {
      name
      description
      id
      createdAt
      updatedAt
    }
  }
`;
export const listTodoIam = /* GraphQL */ `
  query ListTodoIam(
    $filter: ModelTodoIamFilterInput
    $limit: Int
    $nextToken: String
  ) {
    listTodoIam(filter: $filter, limit: $limit, nextToken: $nextToken) {
      items {
        name
        description
        id
        createdAt
        updatedAt
      }
      nextToken
    }
  }
`;
