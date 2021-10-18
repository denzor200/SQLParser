import org.junit.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import ast.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.encodeToJsonElement

data class TestCase(val expected: String, val query: String)

fun ExpressionBinary(left: IExpression, tok: String, right: IExpression): IExpression
{
    return ExpressionProgram(left, mutableListOf(ExpressionOperation(tok, right)))
}

class Test
{
    @Test fun example()
    {
        val CASES = listOf(
            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"ast.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"ast.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"ast.TableName",
                               "tableName":"book"
                            }
                         ]
                      }
                   }
                }
        """, "SELECT * FROM book;"),

            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"ast.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"ast.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"ast.Subquery",
                               "stmt":{
                                  "type":"ast.SelectStmt",
                                  "columns":[
                                     {
                                        "type":"ast.ColumnStar"
                                     }
                                  ],
                                  "from":{
                                     "type":"ast.FromTableOrSubqueryList",
                                     "tableOrSubqueryList":[
                                        {
                                           "type":"ast.TableName",
                                           "tableName":"A"
                                        }
                                     ]
                                  }
                               },
                               "alias":"a_alias"
                            }
                         ]
                      }
                   }
                }
            """, "SELECT * FROM (SELECT * FROM A) a_alias;"),



            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"ast.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"ast.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"ast.TableName",
                               "tableName":"a"
                            },
                            {
                               "type":"ast.TableName",
                               "tableName":"b"
                            },
                            {
                               "type":"ast.TableName",
                               "tableName":"c"
                            }
                         ]
                      }
                   }
                }
            """, "SELECT * FROM a,b,c;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"ast.Column",
                            "expr":{
                               "type":"ast.ExpressionIdentifier",
                               "value":"marvel.id"
                            },
                            "alias":"id"
                         },
                         {
                            "type":"ast.Column",
                            "expr":{
                               "type":"ast.ExpressionIdentifier",
                               "value":"marvel.name"
                            },
                            "alias":"name"
                         },
                         {
                            "type":"ast.Column",
                            "expr":{
                               "type":"ast.ExpressionIdentifier",
                               "value":"marvel.abilities"
                            },
                            "alias":"abilities"
                         }
                      ],
                      "from":{
                         "type":"ast.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"ast.TableName",
                               "tableName":"marvel"
                            }
                         ]
                      }
                   }
                }
            """, "SELECT marvel.id AS id, marvel.name AS name, marvel.abilities AS abilities FROM marvel;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"ast.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"ast.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"ast.TableName",
                               "tableName":"marvel"
                            }
                         ]
                      },
                      "whereClause":{
                         "type":"ast.ExpressionProgram",
                         "first":{
                            "type":"ast.ExpressionProgram",
                            "first":{
                               "type":"ast.ExpressionIdentifier",
                               "value":"id"
                            },
                            "rest":[
                               {
                                  "tok":">",
                                  "operand":{
                                     "type":"ast.ExpressionIntegralLiteral",
                                     "value":1
                                  }
                               }
                            ]
                         },
                         "rest":[
                            {
                               "tok":"AND",
                               "operand":{
                                  "type":"ast.ExpressionProgram",
                                  "first":{
                                     "type":"ast.ExpressionIdentifier",
                                     "value":"id"
                                  },
                                  "rest":[
                                     {
                                        "tok":"<=",
                                        "operand":{
                                           "type":"ast.ExpressionIntegralLiteral",
                                           "value":5
                                        }
                                     }
                                  ]
                               }
                            }
                         ]
                      }
                   }
                }
            """, "SELECT * FROM marvel WHERE id > 1 AND id <=5;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"ast.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"ast.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"ast.TableName",
                               "tableName":"marvel"
                            }
                         ]
                      },
                      "limit":{
                         "type":"ast.ExpressionIntegralLiteral",
                         "value":3
                      }
                   }
                }
            """, "SELECT * FROM marvel LIMIT 3;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"ast.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"ast.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"ast.TableName",
                               "tableName":"marvel"
                            }
                         ]
                      },
                      "limit":{
                         "type":"ast.ExpressionIntegralLiteral",
                         "value":3
                      },
                      "offset":{
                         "type":"ast.ExpressionIntegralLiteral",
                         "value":2
                      }
                   }
                }
            """, "SELECT * FROM marvel LIMIT 3 OFFSET 2;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"ast.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"ast.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"ast.TableName",
                               "tableName":"marvel"
                            }
                         ]
                      },
                      "groupByColumns":[
                         {
                            "type":"ast.ExpressionIdentifier",
                            "value":"name"
                         }
                      ]
                   }
                }
            """, "SELECT * FROM marvel GROUP BY name;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"ast.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"ast.FromTableOrSubqueryList",
                         "tableOrSubqueryList":[
                            {
                               "type":"ast.TableName",
                               "tableName":"marvel"
                            }
                         ]
                      },
                      "sortColumns":[
                         {
                            "type":"ast.ExpressionIdentifier",
                            "value":"name"
                         }
                      ]
                   }
                }
            """, "SELECT * from marvel ORDER BY name;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"ast.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"ast.FromJoinClause",
                         "tableOrSubquery":{
                            "type":"ast.TableName",
                            "tableName":"objects"
                         },
                         "joinOp":"INNER",
                         "joinTableOrSubquery":{
                            "type":"ast.TableName",
                            "tableName":"users"
                         },
                         "joinConstraint":{
                            "type":"ast.ExpressionProgram",
                            "first":{
                               "type":"ast.ExpressionIdentifier",
                               "value":"users.id"
                            },
                            "rest":[
                               {
                                  "tok":"==",
                                  "operand":{
                                     "type":"ast.ExpressionIdentifier",
                                     "value":"objects.id"
                                  }
                               }
                            ]
                         }
                      }
                   }
                }
            """, "SELECT * FROM objects INNER JOIN users ON users.id == objects.id;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"ast.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"ast.FromJoinClause",
                         "tableOrSubquery":{
                            "type":"ast.TableName",
                            "tableName":"objects"
                         },
                         "joinOp":"LEFT",
                         "joinTableOrSubquery":{
                            "type":"ast.TableName",
                            "tableName":"users"
                         },
                         "joinConstraint":{
                            "type":"ast.ExpressionProgram",
                            "first":{
                               "type":"ast.ExpressionIdentifier",
                               "value":"users.id"
                            },
                            "rest":[
                               {
                                  "tok":"==",
                                  "operand":{
                                     "type":"ast.ExpressionIdentifier",
                                     "value":"objects.id"
                                  }
                               }
                            ]
                         }
                      }
                   }
                }
            """, "SELECT * FROM objects LEFT JOIN users ON users.id == objects.id;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"ast.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"ast.FromJoinClause",
                         "tableOrSubquery":{
                            "type":"ast.TableName",
                            "tableName":"objects"
                         },
                         "joinOp":"RIGHT",
                         "joinTableOrSubquery":{
                            "type":"ast.TableName",
                            "tableName":"users"
                         },
                         "joinConstraint":{
                            "type":"ast.ExpressionProgram",
                            "first":{
                               "type":"ast.ExpressionIdentifier",
                               "value":"users.id"
                            },
                            "rest":[
                               {
                                  "tok":"==",
                                  "operand":{
                                     "type":"ast.ExpressionIdentifier",
                                     "value":"objects.id"
                                  }
                               }
                            ]
                         }
                      }
                   }
                }
            """, "SELECT * FROM objects RIGHT JOIN users ON users.id == objects.id;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"ast.ColumnStar"
                         }
                      ],
                      "from":{
                         "type":"ast.FromJoinClause",
                         "tableOrSubquery":{
                            "type":"ast.TableName",
                            "tableName":"objects"
                         },
                         "joinOp":"FULL",
                         "joinTableOrSubquery":{
                            "type":"ast.TableName",
                            "tableName":"users"
                         },
                         "joinConstraint":{
                            "type":"ast.ExpressionProgram",
                            "first":{
                               "type":"ast.ExpressionIdentifier",
                               "value":"users.id"
                            },
                            "rest":[
                               {
                                  "tok":"==",
                                  "operand":{
                                     "type":"ast.ExpressionIdentifier",
                                     "value":"objects.id"
                                  }
                               }
                            ]
                         }
                      }
                   }
                }
            """, "SELECT * FROM objects FULL JOIN users ON users.id == objects.id;"),


            TestCase("""
                {
                   "stmt":{
                      "columns":[
                         {
                            "type":"ast.Column",
                            "expr":{
                               "type":"ast.ExpressionIdentifier",
                               "value":"author.name"
                            }
                         },
                         {
                            "type":"ast.Column",
                            "expr":{
                               "type":"ast.ExpressionFunctionCall",
                               "value":"count",
                               "args":[
                                  {
                                     "type":"ast.ExpressionIdentifier",
                                     "value":"book.id"
                                  }
                               ]
                            }
                         },
                         {
                            "type":"ast.Column",
                            "expr":{
                               "type":"ast.ExpressionFunctionCall",
                               "value":"sum",
                               "args":[
                                  {
                                     "type":"ast.ExpressionIdentifier",
                                     "value":"book.cost"
                                  }
                               ]
                            }
                         }
                      ],
                      "from":{
                         "type":"ast.FromJoinClause",
                         "tableOrSubquery":{
                            "type":"ast.TableName",
                            "tableName":"author"
                         },
                         "joinOp":"LEFT",
                         "joinTableOrSubquery":{
                            "type":"ast.TableName",
                            "tableName":"book"
                         },
                         "joinConstraint":{
                            "type":"ast.ExpressionProgram",
                            "first":{
                               "type":"ast.ExpressionIdentifier",
                               "value":"author.id"
                            },
                            "rest":[
                               {
                                  "tok":"=",
                                  "operand":{
                                     "type":"ast.ExpressionIdentifier",
                                     "value":"book.author_id"
                                  }
                               }
                            ]
                         }
                      },
                      "groupByColumns":[
                         {
                            "type":"ast.ExpressionIdentifier",
                            "value":"author.name"
                         }
                      ],
                      "having":{
                         "type":"ast.ExpressionProgram",
                         "first":{
                            "type":"ast.ExpressionProgram",
                            "first":{
                               "type":"ast.ExpressionFunctionCallStar",
                               "value":"COUNT"
                            },
                            "rest":[
                               {
                                  "tok":">",
                                  "operand":{
                                     "type":"ast.ExpressionIntegralLiteral",
                                     "value":1
                                  }
                               }
                            ]
                         },
                         "rest":[
                            {
                               "tok":"AND",
                               "operand":{
                                  "type":"ast.ExpressionProgram",
                                  "first":{
                                     "type":"ast.ExpressionFunctionCall",
                                     "value":"SUM",
                                     "args":[
                                        {
                                           "type":"ast.ExpressionIdentifier",
                                           "value":"book.cost"
                                        }
                                     ]
                                  },
                                  "rest":[
                                     {
                                        "tok":">",
                                        "operand":{
                                           "type":"ast.ExpressionIntegralLiteral",
                                           "value":500
                                        }
                                     }
                                  ]
                               }
                            }
                         ]
                      },
                      "limit":{
                         "type":"ast.ExpressionIntegralLiteral",
                         "value":10
                      }
                   }
                }
            """,
            """
                SELECT author.name, count(book.id), sum(book.cost) 
                FROM author 
                LEFT JOIN book ON (author.id = book.author_id) 
                GROUP BY author.name HAVING COUNT(*) > 1 AND SUM(book.cost) > 500
                LIMIT 10;
            """)
        )
//


//        var q = Query()
//        q.parse("SELECT marvel.id AS id, marvel.name AS name, marvel.abilities AS abilities FROM marvel;")
//        println(Json.encodeToJsonElement(q))

        for (case in CASES)
        {
            var q = Query()
            q.parse(case.query)
            assertEquals(Json.decodeFromString(case.expected), Json.encodeToJsonElement(q))
        }
    }
}

